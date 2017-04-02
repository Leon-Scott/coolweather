package com.example.leon.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leon.coolweather.db.City;
import com.example.leon.coolweather.db.County;
import com.example.leon.coolweather.db.Province;
import com.example.leon.coolweather.util.HttpUtil;
import com.example.leon.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Leon on 2017/3/31.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_COUNTY =3;

    private int currentLevel;

    private Button button_back;
    private TextView textView_title;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private ProgressDialog progressDialog;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        button_back = (Button) view.findViewById(R.id.id_backButton);
        textView_title = (TextView) view.findViewById(R.id.id_titletext);
        listView = (ListView) view.findViewById(R.id.id_listView);
        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){  //���������ҳ
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weatherId",weatherId);
                    startActivity(intent);
                    //getActivity().finish();
                }
            }
        });
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
    }

    /*查询全国�??有的省份，优先从数据库查询，若没有则从网上查�??*/
    private void queryProvinces() {
        textView_title.setText("�й�");
        button_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,LEVEL_PROVINCE);
        }
    }

    /*查询当前省份的所有市的数据，优先从数据库查询，若没有则从服务器查�??*/
    private void queryCities(){

        cityList = DataSupport.where("provinceId = ?",String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if(cityList.size() > 0){
            textView_title.setText(selectedProvince.getProvinceName());
            button_back.setVisibility(View.VISIBLE);
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(address,LEVEL_CITY);
        }
    }

    /*查询当前市的�??有县的数据，优先从数据库查询，若没有则从服务器查�??*/
    private void queryCounties(){

        //button_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?",String.valueOf(selectedCity.getCityCode())).find(County.class);
        if(countyList.size() > 0){
            textView_title.setText(selectedCity.getCityName());
            dataList.clear();
            for (County county:countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() +"/" + selectedCity.getCityCode();
            queryFromServer(address,LEVEL_COUNTY);
        }
    }

     /*从服务器上查询需要的数据*/
    private void queryFromServer(String url, final int type){
        //显示�??个进度对话框，服务器查询�??要时间，不可操作
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"��������������ʧ��",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Boolean result = false;
                String responseData = response.body().string();
                //Log.d("ChooseFragment1:",response.body().string());
                if(type == LEVEL_PROVINCE){
                    result = Utility.handleProvincesResponse(responseData);
                }else if(type == LEVEL_CITY){
                    result = Utility.handleCitiesResponse(responseData,selectedProvince.getProvinceCode());
                }else if(type == LEVEL_COUNTY){
                    result = Utility.handleCountiesResponse(responseData,selectedCity.getCityCode());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(type == LEVEL_PROVINCE)
                                queryProvinces();
                            else if(type ==LEVEL_CITY)
                                queryCities();
                            else if(type == LEVEL_COUNTY)
                                queryCounties();
                        }
                    });
                }
                else{
                    Log.d("ChooseFragment:",response.body().toString());
                }
            }
        });
    }

    /*显示加载对话�??*/
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("���ڼ���...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*取消加载对话�??*/
    private void closeProgressDialog(){
        if(progressDialog != null)
            progressDialog.dismiss();
    }

}
