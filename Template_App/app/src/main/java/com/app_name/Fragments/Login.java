package com.app_name.Fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.app_name.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class Login extends Fragment {
    private View myView;
    private Button logginButton;
    private TextView forgotPassword;
    private TextView newAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_login, container, false);   // get the view of the fragment (do not delete or move)
        constructViews();


        logginButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_manager, null));  // only to change page

        forgotPassword.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_forgotPassword, null));  // only to change page

        newAccount.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_createNewAccount, null));  // only to change page


        return myView;
    }


    private void constructViews() {        // initialize all the variables in an organized way

        logginButton = myView.findViewById(R.id.loginButton_login);
        forgotPassword = myView.findViewById(R.id.forgotPasswordTextView_login);
        newAccount = myView.findViewById(R.id.createNewAccountTextView_login);
    }
}
