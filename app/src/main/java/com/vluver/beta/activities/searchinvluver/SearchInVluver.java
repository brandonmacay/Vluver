package com.vluver.beta.activities.searchinvluver;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.vluver.beta.R;
import com.vluver.beta.adapter.SearchUserAdapter;
import com.vluver.beta.model.SearchUser;
import com.vluver.beta.serviceVolley.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.vluver.beta.Direccion.urlgeneral;


public class SearchInVluver extends AppCompatActivity implements  MaterialSearchBar.OnSearchActionListener{
    MaterialSearchBar searchBar;
    public RecyclerView mRVFish;
    public SearchUserAdapter mAdapter;
    List<SearchUser> data;
    ProgressDialog finding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_in_vluver);
        searchBar = findViewById(R.id.searchBarMain);
        data=new ArrayList<>();
        mRVFish = (RecyclerView) findViewById(R.id.rv_resultados);
        mAdapter = new SearchUserAdapter(SearchInVluver.this, data);
        mRVFish.setAdapter(mAdapter);
        mRVFish.setLayoutManager(new LinearLayoutManager(SearchInVluver.this));
        finding = new ProgressDialog(SearchInVluver.this,R.style.AppCompatAlertDialogStyle);
        searchBar.setOnSearchActionListener(this);
        searchBar.inflateMenu(R.menu.main);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        searchBar.enableSearch();
    }
    private void searchUser(String text){
        finding.setMessage("Buscando...");
        finding.setCancelable(false);
        finding.show();
        String url = urlgeneral+"search/user_search.php?searchQuery="+text+"&usuario="+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        VolleySingleton.
                getInstance(SearchInVluver.this).
                addToRequestQueue(
                        new JsonObjectRequest(
                                Request.Method.GET, url, (JSONObject) null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Procesar la respuesta Json
                                        procesarRespuesta(response);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        finding.dismiss();
                                        Toast.makeText(SearchInVluver.this, "Internet no disponible:\t"+error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
                );

    }
    private void procesarRespuesta(JSONObject response) {
        try {

            if (data != null){
                data.clear();
            }
            boolean error = response.getBoolean("error");

            if (!error) {
                JSONObject postt = response.getJSONObject("user");

                JSONArray pray = postt.getJSONArray("email");
                for (int i = 0; i < pray.length(); i++) {


                    JSONArray objPid = postt.getJSONArray("fullnames");
                    String fullnames = objPid.getString(i);

                    JSONArray objname = postt.getJSONArray("email");
                    String email = objname.getString(i);

                    JSONArray objuid = postt.getJSONArray("unique_id");
                    String uid = objuid.getString(i);

                    JSONArray objuserimage = postt.getJSONArray("avatar");
                    String avatar = objuserimage.getString(i);

                    //JSONArray statefollow = postt.getJSONArray("statefollow");
                    //int statefollower = statefollow.getInt(i);

                    //JSONArray privacyUser = postt.getJSONArray("privacy");
                    //int privacy = privacyUser.getInt(i);
                    SearchUser userData = new SearchUser();

                    userData.userName = fullnames;
                    userData.userEmail = email;
                    userData.userUID = uid;
                    userData.userAvatar = avatar;
                   // userData.userPrivacy = privacy;
                   // userData.statefollow = statefollower;
                    data.add(userData);
                }

                mAdapter.notifyDataSetChanged();
                finding.dismiss();
                mRVFish.setAdapter(mAdapter);
                mRVFish.setLayoutManager(new LinearLayoutManager(SearchInVluver.this));


            } else {
                finding.dismiss();
                String errorMsg = response.getString("error_msg");
                Toast.makeText(SearchInVluver.this, ""+errorMsg, Toast.LENGTH_SHORT).show();

            }

        } catch (JSONException e) {
            finding.dismiss();
            Toast.makeText(SearchInVluver.this, ""+e, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled){
            finish();
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        searchUser(text.toString());

    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                finish();
                break;
        }
    }
}
