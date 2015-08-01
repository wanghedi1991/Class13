package edu.gatech.wang.hedi.classthirteen.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import edu.gatech.wang.hedi.classthirteen.Constants;
import edu.gatech.wang.hedi.classthirteen.R;

/**
 * Created by Hedi Wang on 2015/8/1.
 */
public class UpdateInfoDialogFragment extends DialogFragment {

    UpdateDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_update_info_dialog, null);
        final EditText nameView = (EditText) view.findViewById(R.id.name);
        String name = getActivity().getSharedPreferences(Constants.APPNAME, 0).getString(Constants.NAME_SAVED, null);
        if (name != null && name.length() > 0) {
            nameView.setText(name);
        }
        final EditText orgView = (EditText) view.findViewById(R.id.org);
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogPositiveClick(nameView.getText().toString().trim(), orgView.getText().toString().trim());
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (UpdateDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UpdateDialogListener");
        }
    }

    public interface UpdateDialogListener {
        void onDialogPositiveClick(String name, String org);
    }
}
