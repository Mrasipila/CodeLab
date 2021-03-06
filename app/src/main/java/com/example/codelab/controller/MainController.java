package com.example.codelab.controller;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Adapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.codelab.Constants;
import com.example.codelab.GameAPI;
import com.example.codelab.Injection;
import com.example.codelab.model.ContainerJSON;
import com.example.codelab.view.MainActivity;
import com.example.codelab.view.MyAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainController  implements SearchView.OnQueryTextListener {


    private SharedPreferences SP_cache;
    private Gson gson;
    private MainActivity view;
    private SearchView searchView;
    private MyAdapter adapter;

    public MainController(MainActivity mainactivity, Gson gson, SharedPreferences sharedpreferences){
        this.view = mainactivity;
        this.gson = gson;
        this.SP_cache = sharedpreferences;
    }

    public void onStart(){
        List<ContainerJSON> B = DataListfromCache();
        if(B != null) {
            view.showList(B);
        } else {
            makeApiCall();
        }
    }

    private List<ContainerJSON> DataListfromCache() {
        String collection = SP_cache.getString(Constants.KEY_SHARED_PREF_CLASSES,null);
        if(collection == null){
            return null;
        } else {
            Type ListContainer = new TypeToken<List<ContainerJSON>>() {
            }.getType();
            return gson.fromJson(collection, ListContainer);
        }
    }

    private void makeApiCall(){

        Call<List<ContainerJSON>> call = Injection.My_Api().getClassesInfo();
        call.enqueue(new Callback<List<ContainerJSON>>() {
            @Override
            public void onResponse(Call<List<ContainerJSON>> call, Response<List<ContainerJSON>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<ContainerJSON> A = response.body();
                    Log.d("AAAAAAA",A.get(10).getDescription());
                    saveList(A);
                    view.showList(A);
                } else {
                    view.showError();
                }
            }

            @Override
            public void onFailure(Call<List<ContainerJSON>> call, Throwable t) {
                view.showFailure();
            }
        });

    }

    private void saveList(List<ContainerJSON> from){
        String jsonString = gson.toJson(from);
        SP_cache.edit().putString("JSON",jsonString).apply();
        Toast.makeText(view.getApplicationContext(),"List Saved", Toast.LENGTH_SHORT).show();
    }

    public void setSearchViewAndListener(SearchView searchView){
        this.searchView = searchView;
        searchView.setOnQueryTextListener(this);
    }

    public void setAdapter(MyAdapter adapter){
        this.adapter = adapter;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String userInput = newText.toLowerCase();
        List<ContainerJSON> newList = new ArrayList<>();
        List<ContainerJSON> A = DataListfromCache();

        for(ContainerJSON i : A) {
            if(i.getName().toLowerCase().contains(userInput)){
                newList.add(i);
            }
        }
        adapter.updateList(newList);
        return true;
    }

    public void onItemClick(ContainerJSON item){
        view.navigateToDetails(item);
    }

   /* public void onButtonClick(){

    }*/
}
