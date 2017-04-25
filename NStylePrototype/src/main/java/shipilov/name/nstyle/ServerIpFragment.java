package shipilov.name.nstyle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by HOME on 20.04.2017.
 */

public class ServerIpFragment extends Fragment {

    private EditText ipEdit;
    private EditText adminIpEdit;
    private Button connectButton;

    interface OnConnectListener {
        void onConnect(String ip, String adminIp);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.server_ip, container, false);

        ipEdit = (EditText) rootView.findViewById(R.id.ipInput);
        adminIpEdit = (EditText) rootView.findViewById(R.id.adminIpInput);

        String ip = getStoredServerIp(getActivity());
        if (ip != null)
            ipEdit.setText(ip);

        String adminIp = getStoredAdminServerIp(getActivity());
        if (adminIp != null)
            adminIpEdit.setText(adminIp);

        connectButton = (Button) rootView.findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStoredServerIp(getActivity(), ipEdit.getText().toString());
                setStoredAdminServerIp(getActivity(), adminIpEdit.getText().toString());
                ((OnConnectListener)getActivity()).onConnect(ipEdit.getText().toString(), adminIpEdit.getText().toString());
            }
        });

        return rootView;
    }

    public String getStoredServerIp(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString("ip", null);
    }

    public void setStoredServerIp(Activity activity, String ip) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ip", ip);
        editor.commit();
    }

    public String getStoredAdminServerIp(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString("adminIp", null);
    }

    public void setStoredAdminServerIp(Activity activity, String ip) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("adminIp", ip);
        editor.commit();
    }

}
