package com.cclk.signindemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cclk.signindemo.util.PrefUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SignInActivity";
    private LinearLayout layout_profile;
    private TextView txtName, txtEmail;
    private ImageView profilePic;
    private SignInButton googleSignInButton;
    private Button signInButton, signoutButton;
    private LoginButton fbLoginButton;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    private String flag = "";

    private Intent next;
    private Bundle facebookData;

    // Facebook callback manager
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_sign_in);

        uiInitializing();

        /*LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "AccessToken : " + AccessToken.getCurrentAccessToken());
                        // App code
                        next = new Intent(SignInActivity.this, HomeActivity.class);
                        startActivity(next);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });*/

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess : " + loginResult);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject,
                                                    GraphResponse response) {

                                // Getting FB User Data
                                facebookData = getFacebookData(jsonObject);

                                next = new Intent(SignInActivity.this, HomeActivity.class);
                                next.putExtras(facebookData);
                                startActivity(next);

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(SignInActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(SignInActivity.this);
                }
                builder.setTitle("Connectivity error")
                        .setMessage("Failed to connect facebook service")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


                deleteAccessToken();
            }
        });

    }

    /**
     * Initialise UI components
     */
    private void uiInitializing() {
        layout_profile = (LinearLayout)findViewById(R.id.layout_profile_section);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView)findViewById(R.id.txtEmail);
        profilePic = (ImageView)findViewById(R.id.image_profile);
        signoutButton = (Button)findViewById(R.id.signout_button);
        googleSignInButton = (SignInButton)findViewById(R.id.google_signin_button);
        signInButton = (Button)findViewById(R.id.google_sign_in_button);
        fbLoginButton = (LoginButton)findViewById(R.id.facebook_login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        googleSignInButton.setOnClickListener(this);
        signoutButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        fbLoginButton.setOnClickListener(this);

        layout_profile.setVisibility(View.GONE);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.google_signin_button:
                flag = "GL";
                googleSignIn();
                break;

            case R.id.signout_button:
                flag = "GL";
                googleSignOut();
                break;

            case R.id.google_sign_in_button:
                flag = "GL";
                googleSignIn();
                break;

            case R.id.facebook_login_button:
                flag = "FB";
                // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Sign In to google
     */
    private void googleSignIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    /**
     * Sign out from google
     */
    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUi(false);
            }
        });
    }

    /**
     * Handle response - google sign in
     * @param googleSignInResult
     */
    private void handleResult(GoogleSignInResult googleSignInResult) {
        if(googleSignInResult.isSuccess()) {
            GoogleSignInAccount account = googleSignInResult.getSignInAccount();

            String name = account.getDisplayName();
            String email = account.getEmail();

            txtName.setText(name);
            txtEmail.setText(email);

            if(account.getPhotoUrl() != null) {
                String imageUrl = account.getPhotoUrl().toString();
                Picasso.with(this).load(imageUrl).placeholder(R.drawable.placeholder).resize(100, 100).centerCrop().into(profilePic);
            }

            updateUi(true);
        } else {
            updateUi(false);
        }
    }

    /**
     * Update UI according to sign in status
     *
     * @param isLogin
     */
    private void updateUi(boolean isLogin){
        if(isLogin) {
            layout_profile.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.GONE);
            fbLoginButton.setVisibility(View.GONE);
        } else {
            layout_profile.setVisibility(View.GONE);
            googleSignInButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            fbLoginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(flag.equalsIgnoreCase("GL")) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(googleSignInResult);
        } else if (flag.equalsIgnoreCase("FB")) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     *  get facebook information
     * @param object
     * @return
     */
    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();

        try {
            String id = object.getString("id");
            URL profile_pic;
            try {
                profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));


//            PrefUtil.saveFacebookUserInfo(object.getString("first_name"),
//                    object.getString("last_name"),object.getString("email"),
//                    object.getString("gender"), profile_pic.toString());

        } catch (Exception e) {
            Log.d(TAG, "BUNDLE Exception : " + e.toString());
        }

        return bundle;
    }

    /**
     * Delete access token
     */
    private void deleteAccessToken() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    //User log out
                    PrefUtil.clearToken();
                    LoginManager.getInstance().logOut();
                }
            }
        };
    }
}
