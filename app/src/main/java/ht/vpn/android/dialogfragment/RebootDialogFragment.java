package ht.vpn.android.dialogfragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ht.vpn.android.R;

public class RebootDialogFragment extends DialogFragment {

    private static final String TAG = "reboot_dialog";

    public static RebootDialogFragment show(FragmentManager fm) {
        try {
            Fragment fragment = fm.findFragmentByTag(TAG);
            if (fragment == null) {
                RebootDialogFragment dialogFragment = new RebootDialogFragment();
                dialogFragment.show(fm, TAG);
                return dialogFragment;
            }
            return (RebootDialogFragment) fragment;
        } catch (IllegalStateException e) {
            // Catch 'Can not perform this action after onSaveInstanceState'
        }

        return null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.reboot_message);
        dialogBuilder.setTitle(R.string.reboot_title);
        setCancelable(false);
        return dialogBuilder.create();
    }

}
