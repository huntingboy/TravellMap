package com.nomad.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nomad.travellmap.R;
import com.nomad.web.HttpJson;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     *
     */
    private int i = 0;
    private double latitude;
    private double longitude;

    private static final String[] DUMMY_CREDENTIALS = new String[]{

    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button weiboSignInButton = (Button) findViewById(R.id.weibo_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        weiboSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                weibo.setPlatformActionListener(new PlatformActionListener() {
                    @Override
                    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                        //遍历Map
                        Iterator iterator = hashMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry = (Map.Entry) iterator.next();
                            Object key = entry.getKey();
                            Object value = entry.getValue();
                            Log.d("Amap", "loginactivity->oncreate->onclick()->oncomplete()===key:" + key + ", value:" + value);
                            //todo 1.得到邮箱 密码 2.edittext设置邮箱 密码  3.attemptLogin();
                        }
                    }

                    @Override
                    public void onError(Platform platform, int i, Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {

                    }
                });
                weibo.showUser(null); //执行登录，登录后在回调里面获取用户资料
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4 && !password.contains(" ");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final int RESULT_LOGIN_OK = 2;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10*1000);//设置请求超时10秒
            HttpConnectionParams.setSoTimeout(httpParameters, 10*1000); //设置等待数据超时10秒
            HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);

            String protocol = getResources().getString(R.string.url_protocol);
            String host = getResources().getString(R.string.url_host);
            String port = getResources().getString(R.string.url_port);
            String path = getResources().getString(R.string.url_path_user);

            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());  //更新登录已注册用户的位置信息和登录时间
            //get方式提交
            String url = protocol + "://" + host + ":" + port + "/" + path
                    + "?" + "username=" + mEmail + "&password=" + mPassword +
                    "&latitude=" + latitude + "&longitude=" + longitude + "&dateTime=" + URLEncoder.encode(dateTime) + "&method=login";
            Log.d("Amap", "get url===>" + url);
            HttpJson httpJson = new HttpJson(url);
            try {
                String stringJson = httpJson.doHttpGetJson();
                Log.d("Amap", "服务器登录返回数据====》" + stringJson);
                if (httpJson.getResultCode() != 200) {
                    Log.d("Amap", "====LoginAcitivity->UserLoginTast->doInBackground()登录resultcode:" + httpJson.getResultCode());
                    return false;
                } else {
                    //1.将Json数据中User解析出来 2.得到username和password放到DUMMY_CREDENTIALS数组中
                    JSONObject jsonObject = new JSONObject(stringJson);
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("password");
                    //DUMMY_CREDENTIALS[i++] = username + ":" + password;
                    if (username != null && password != null) {
                        return true;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                //return false;
            }

            /**post方式
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", mEmail);
                jsonObject.put("password", password);
                String jsonString = jsonObject.toString();
                String url = protocol + "://" + host + ":" + port + "/" + path
                        + "/";
                HttpJson httpJson = new HttpJson(jsonString, url);
                String stringJson = httpJson.doHttpPostJson();
                Log.d("Amap", "服务器登录返回数据====》" + stringJson);
                if (httpJson.getResultCode() != 200) {
                    Toast.makeText(LoginActivity.this, "错误代码：" + httpJson.getResultCode(), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    //1.将Json数据中User解析出来 2.得到username和password放到DUMMY_CREDENTIALS数组中
                    JSONObject jsonObject = new JSONObject(stringJson);
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("password");
                    DUMMY_CREDENTIALS[0] = username + ":" + password;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            //get方式提交
            String url1 = protocol + "://" + host + ":" + port + "/" + path
                    + "?" + "username=" + mEmail + "&password=" + mPassword +
                    "&latitude=" + latitude + "&longitude=" + longitude + "&dateTime=" + URLEncoder.encode(dateTime) + "&method=register";
            Log.d("Amap", "get url1===>" + url1);
            try {
                HttpJson httpJson1 = new HttpJson(url1);
                String stringJson = httpJson1.doHttpGetJson();
                Log.d("Amap", "Loginactivity->userlogintast->doninbackground->服务器登录返回数据====》" + stringJson);
                Log.d("Amap", "Loginactivity->userlogintast->doninbackground->结果状态码：" + httpJson.getResultCode());
                if (httpJson.getResultCode() != 200) {
                    Log.d("Amap", "====LoginAcitivity->UserLoginTast->doInBackground()注册resultcode:" + httpJson.getResultCode());
                    return false;
                } else {
                    //1.将Json数据中User解析出来 2.得到username和password放到DUMMY_CREDENTIALS数组中
                    JSONObject jsonObject = new JSONObject(stringJson);
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("password");
                    Log.d("Amap", "====LoginAcitivity->UserLoginTast->doInBackground()注册username=" + username + ",password=" + password);
                    if (username == null || password == null) {
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                Log.d("Amap", "注册结果：json解析错误，可能根据空对象构造json!");
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(LoginActivity.this, "登录/注册成功！", Toast.LENGTH_SHORT).show();
                //1.将用户名返回到MainAcitivity.java显示出来
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("username", mEmail);
                intent.putExtra("bundle", bundle);
                LoginActivity.this.setResult(RESULT_LOGIN_OK, intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

