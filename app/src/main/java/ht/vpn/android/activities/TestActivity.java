package ht.vpn.android.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import ht.vpn.android.LaunchVPN;
import ht.vpn.android.Preferences;
import ht.vpn.android.R;
import ht.vpn.android.VPNhtApplication;
import ht.vpn.android.VpnProfile;
import ht.vpn.android.content.VPNHTConfig;
import ht.vpn.android.dialogfragment.ConnectingDialogFragment;
import ht.vpn.android.network.IPService;
import ht.vpn.android.network.VPNService;
import ht.vpn.android.network.responses.Server;
import ht.vpn.android.network.responses.ServersResponse;
import ht.vpn.android.utils.NetworkUtils;
import ht.vpn.android.utils.PrefUtils;
import ht.vpn.android.utils.ThreadUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Created by wc on 2016/4/21.
 */
public class TestActivity extends BaseActivity implements VpnStatus.StateListener,VpnStatus.LogListener {

    private Boolean mConnected = false;
    private Activity mActivity = this;
    private Boolean mShowsConnected = false;
    private String mDetectedCountry;
    private VpnStatus.ConnectionStatus mCurrentVPNState = VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED;
    private LatLng mCurrentLocation;
    private GoogleMap mMap;
    private HashMap<Marker, Integer> mMarkers = new HashMap<>();
    private Server server;
    private boolean firewall = true;
    private Marker mCurrentPosMarker;
    private OpenVPNService mService;
    private SupportMapFragment mMapFragment;
    private android.support.v4.app.FragmentTransaction mFragmentTransaction;
    private VPNService.Client mVPNAPI;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_test);
        VpnStatus.addLogListener(this);
        VpnStatus.addStateListener(this);

        IPService.get().status(mIPCallback);

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mMapFragment = new SupportMapFragment();
        mFragmentTransaction.add(R.id.mapFragment, mMapFragment);

        mMapFragment.getMapAsync(mMapReadyCallback);

        server = new Server();
        server.country = "中国";
        server.countryCode = "1";
        server.hostname = "0.0.0.0";
        server.location = "北京";
        server.longlat = new double[]{40 , 118};

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if(!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMarkerClickListener(mMarkerClickListener);
            updateMapLocation();

            if(mActivity != null) {
                mVPNAPI = VPNService.get(PrefUtils.get(mActivity, Preferences.USERNAME, ""), PrefUtils.get(mActivity, Preferences.PASSWORD, ""));
                mVPNAPI.servers(mServersCallback);
            }
        }
    };

    private Callback<ServersResponse> mServersCallback = new Callback<ServersResponse>() {
        @Override
        public void success(ServersResponse serversResponse, Response response) {
            if(mActivity == null) return;

//            mServers = (ArrayList<Server>) serversResponse.servers;
            for(Marker marker : mMarkers.keySet()) {
                marker.remove();
            }

            ArrayList<String> spinnerList = new ArrayList<>();
            for(Server server : serversResponse.servers) {
                Marker marker;
                if (server.getCoordinates() != null) {
                    marker = mMap.addMarker(new MarkerOptions().position(server.getCoordinates()).icon(BitmapDescriptorFactory.fromResource(R.drawable.server)));
                } else {
                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.server)));
                    marker.setVisible(false);
                }
                mMarkers.put(marker, spinnerList.size());

                spinnerList.add(server.country);
            }

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, R.layout.simple_spinner_item, spinnerList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mLocationSpinner.setAdapter(arrayAdapter);
                }
            });

            if(mShowsConnected) {
                processServers();
            }
            updateMapLocation();
        }

        @Override
        public void failure(RetrofitError error) {
            Timber.e(error, error.getMessage());
            Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show();

//            if(error.getResponse() != null && error.getResponse().getStatus() == 401)
//                mActivity.startLoginActivity();
        }
    };

    private GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if(mMarkers.containsKey(marker)) {
//                mLocationSpinner.setSelection(mMarkers.get(marker));
            }

            return true;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private Callback<IPService.Data> mIPCallback = new Callback<IPService.Data>() {
        @Override
        public void success(final IPService.Data data, Response response) {
            if(mActivity == null) return;

            if(response != null && response.getStatus() == 200) {
                mShowsConnected = data.connected;
                mDetectedCountry = data.country;

                if(!mShowsConnected && mCurrentVPNState.equals(VpnStatus.ConnectionStatus.LEVEL_CONNECTED)) {
                    IPService.get().status(mIPCallback);
                    return;
                }

                String location = null;
                if (mShowsConnected) {

                } else {

                    try {
                        Geocoder coder = new Geocoder(mActivity);
                        List<Address> addressList;
                        if (!data.hasCoordinates()) {
                            addressList = coder.getFromLocationName("Country: " + data.country, 1);
                        } else {
                            addressList = coder.getFromLocation(data.getLat(), data.getLng(), 1);
                        }
                        if (addressList != null && addressList.size() > 0) {
                            Address address = addressList.get(0);
                            if (address.getLocality() == null) {
                                location = address.getCountryName();
                            } else {
                                location = String.format("%s, %s", address.getLocality(), address.getCountryCode());
                            }

                            if (address.hasLatitude() && address.hasLongitude())
                                mCurrentLocation = new LatLng(address.getLatitude(), address.getLongitude());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (location == null && data.country != null) {
                    Locale locale = new Locale("", data.country);
                    location = locale.getDisplayCountry();
                }

                final String finalLocation = location;

                processServers();
                updateMapLocation();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
                Log.i("ERROR", "返回码是401，返回信息：" + error.getResponse());
            }
        }
    };

    private void processServers() {
        Marker[] markers = mMarkers.keySet().toArray(new Marker[mMarkers.size()]);

            if (server.getCoordinates() != null && server.countryCode != null && server.countryCode.equals(mDetectedCountry)) {
                mCurrentLocation = server.getCoordinates();

            }

    }

    private void updateMapLocation() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMap != null && mCurrentLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 5));
                    if (mCurrentPosMarker == null) {
                        mCurrentPosMarker = mMap.addMarker(new MarkerOptions().position(mCurrentLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)));
                    } else {
                        mCurrentPosMarker.setPosition(mCurrentLocation);
                        mCurrentPosMarker.setVisible(true);
                    }
                }
            }
        });
    }

    public void click(View v){
        switch (v.getId()){
            case R.id.bt_submit:
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        VpnStatus.clearLog();

                        PrefUtils.save(mActivity, Preferences.LAST_CONNECTED_HOSTNAME, server.hostname);
                        PrefUtils.save(mActivity, Preferences.LAST_CONNECTED_COUNTRY, server.country);
                        PrefUtils.save(mActivity, Preferences.LAST_CONNECTED_FIREWALL, firewall);
                        ConfigParser configParser = new ConfigParser();
                        try {
                            configParser.parseConfig(new StringReader(VPNHTConfig.generate(PrefUtils.getPrefs(mActivity), server, firewall)));
                            VpnProfile profile = configParser.convertProfile();
                            profile.mName = server.country;
                            profile.mUsername = "xxxx";
                            profile.mPassword = "xxxx";
                            profile.mAuthenticationType = VpnProfile.TYPE_USERPASS;
                            ProfileManager.setTemporaryProfile(profile);


                            Intent vpnPermissionIntent = VpnService.prepare(mActivity);

                            if (vpnPermissionIntent != null) {
                                Intent intent = new Intent(mActivity, LaunchVPN.class);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUIDString());
                                intent.putExtra(LaunchVPN.EXTRA_HIDELOG, true);
                                startActivity(intent);
                            } else {
                                VPNLaunchHelper.startOpenVpn(profile, VPNhtApplication.getAppContext());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ConfigParser.ConfigParseError configParseError) {
                            configParseError.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
                break;
        }
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level) {
        mConnected = level.equals(VpnStatus.ConnectionStatus.LEVEL_CONNECTED);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void newLog(VpnStatus.LogItem logItem) {
        Timber.i("%s: %s", logItem.getLogLevel(), logItem.getString(mActivity));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VpnStatus.removeStateListener(this);
        VpnStatus.removeLogListener(this);
    }
}
