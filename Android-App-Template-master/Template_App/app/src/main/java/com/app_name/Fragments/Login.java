package com.app_name.Fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.amazonaws.amplify.generated.graphql.CreateTodoMutation;
import com.amazonaws.amplify.generated.graphql.ListTodosQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.app_name.R;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import javax.annotation.Nonnull;

import type.CreateTodoInput;


public class Login extends Fragment {
    private View myView;
    private Button button;
    private TextView forgotPassword;
    private TextView newAccount;

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_login, container, false);   // get the view of the fragment (do not delete or move)
        constructViews();


        button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_createNewAccount, null));  // only to change page
        forgotPassword.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_forgotPassword, null));  // only to change page

        newAccount.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_login_to_createNewAccount, null));  // only to change page


        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getContext())
                .awsConfiguration(new AWSConfiguration(getContext()))
                .build();


        //runMutation();
        runQuery();
        return myView;
    }

    private void constructViews() {        // initialize all the variables in an organized way

        button = myView.findViewById(R.id.loginButton_login);
        forgotPassword = myView.findViewById(R.id.forgotPasswordTextView_login);
        newAccount = myView.findViewById(R.id.createNewAccountTextView_login);
    }

    public void runMutation(){
        CreateTodoInput createTodoInput = CreateTodoInput.builder().
                name("Use AppSync").
                description("Realtime and Offline").
                build();

        mAWSAppSyncClient.mutate(CreateTodoMutation.builder().input(createTodoInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateTodoMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTodoMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTodoMutation.Data> response) {
            Log.i("Results", "Added Todo");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    public void runQuery(){
        mAWSAppSyncClient.query(ListTodosQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(todosCallback);
    }

    private GraphQLCall.Callback<ListTodosQuery.Data> todosCallback = new GraphQLCall.Callback<ListTodosQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTodosQuery.Data> response) {
            Log.i("Results", response.data().listTodos().items().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

}
