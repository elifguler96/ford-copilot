package com.elifguler.fordcopilot;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeTabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeTabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int dataCount = 0;
    private static final String TAG = "HomeTabFragment";
    private VehicleManager mVehicleManager;
    private TextView mLocationInfoTextView;
    private TextView mSpeedInfoTextView;
    private TextView mSpeedLimitInfoTextView;
    private TextView mFuelPercentageInfoTextView;
    private TextView mFuelOutageInfoTextView;
    private AlertDialog alertDialog;

    private List<Pair<Double, Long>> timeSpeedList = new ArrayList<>();

    private VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {

        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
//                    String str = highestSpeedEditText.getText().toString();
//                    if (str.isEmpty()) {
//                        str = "200";
//                    }
//                    if (speed.getValue().doubleValue() > Double.parseDouble(str)) {
//                        highestSpeedWarningTextView.setText("WARNING!! " +  speed.getValue()
//                                .doubleValue());
//                    } else {
//                        highestSpeedWarningTextView.setText(" " + speed.getValue().doubleValue());
//                    }
                    if (dataCount++ % 10 != 0) return;
                    mSpeedInfoTextView.setText("" + speed.getValue().doubleValue());

                    timeSpeedList.add(Pair.create(speed.getValue().doubleValue(), System
                            .currentTimeMillis() / 1000));
                    double averageAcceleration = 0;
                    final double g = 9.81;
                    for (Pair p : timeSpeedList) {
                        Log.e("f", "" + p.first);
                        Log.e("s", "" + p.second);
                    }
                    for (int i = timeSpeedList.size() - 1; i > Math.max(timeSpeedList.size() - 10,
                            0); i--) {
                        Pair<Double, Long> p1 = timeSpeedList.get(i);
                        Pair<Double, Long> p2 = timeSpeedList.get(i - 1);
                        averageAcceleration += (p1.first - p2.first) / (p1.second -
                                p2.second);
                    }
                    averageAcceleration /= Math.min(10, timeSpeedList.size());
                    Log.e("avgAcceleration", "" + averageAcceleration);
                    if (averageAcceleration > g && (alertDialog == null || !alertDialog
                            .isShowing())) {
                        alertDialog = new AlertDialog.Builder(getContext())
                                .setMessage("You are accelerating too fast, this is bad for your " +
                                        "car.").create();
                        alertDialog.show();
                        // Too much acceleration
                    } else if (averageAcceleration < -0.8 * g && (alertDialog == null ||
                            !alertDialog.isShowing())) {
                        alertDialog = new AlertDialog.Builder(getContext())
                                .setMessage("You are breaking too harsh, this is bad for your " +
                                        "car.").create();
                        alertDialog.show();
                    } else if (averageAcceleration < -1.2 * g) {
                        RestInterfaceController controller = Controller.getController();
                        Call<Integer> call = controller.sendEmergencyText();
                        call.enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {

                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {

                            }
                        });
                    }
                }
            });
        }
    };

    private Latitude.Listener mLatitudeListener = new Latitude.Listener() {
        int dataCount = 0;

        @Override
        public void receive(Measurement measurement) {
            final Latitude lat = (Latitude) measurement;
            try {
                Longitude lng = (Longitude) mVehicleManager.get(Longitude.class);
                final double latd = lat.getValue().doubleValue();
                final double lngd = lng.getValue().doubleValue();

                RestInterfaceController controller = Controller.getController();
                Call<SpeedLimit> call = controller.speedLimit(new HashMap<String, Double>(){{
                    put("lat", latd);
                    put("lng", lngd);
                }});
                call.enqueue(new Callback<SpeedLimit>() {
                    @Override
                    public void onResponse(Call<SpeedLimit> call, Response<SpeedLimit> response) {
                        SpeedLimit data = response.body();
                        SpannableStringBuilder text = new SpannableStringBuilder();
                        if (data.roadName != null && data.roadType != null) {
                            text.append("You are driving on <b>").append(data.roadName).append
                                    ("</b> which is a <b>").append(data.roadType).append("</b> " +
                                    "road. ");
                        } else if (data.roadName != null) {
                            text.append("You are driving on <b>").append(data.roadName).append
                                    ("</b>.");
                        } else if (data.roadType != null) {
                            text.append("You are driving on a <b>").append(data.roadType).append
                                    ("</b> road. ");
                        } else {
                            mLocationInfoTextView.setText(R.string.location_not_available);
                            return;
                        }

                        Pattern p = Pattern.compile("<b>.*?</b>", Pattern.CASE_INSENSITIVE);
                        boolean stop = false;
                        while (!stop)
                        {
                            Matcher m = p.matcher(text.toString());
                            if (m.find()) {
                                text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                text.delete(m.end()-4, m.end());
                                text.delete(m.start(), m.start() + 3);
                            }
                            else
                                stop = true;
                        }

                        mLocationInfoTextView.setText(text);

                        text = new SpannableStringBuilder();
                        if (data.speedLimit != -1) {
                            mSpeedLimitInfoTextView.setText("" + data.speedLimit);
                        } else {
                            mSpeedLimitInfoTextView.setText(R.string.not_available);
                        }
                    }

                    @Override
                    public void onFailure(Call<SpeedLimit> call, Throwable t) {

                    }
                });

            } catch(NoValueException e) {
                Log.w(TAG, "The vehicle may not have made the measurement yet");
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "The measurement type was not recognized");
            }

        }
    };

    private void logData(String type, Serializable data) {
        RestInterfaceController controller = Controller.getController();
        DataLog dataLog = new DataLog();
        dataLog.type = "fuel-km";
        dataLog.data = data;
        Call<Void> call = controller.logData(dataLog);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    private Odometer.Listener mOdometerListener = new Odometer.Listener() {
        double lastMeasurement = 0;

        @Override
        public void receive(Measurement measurement) {
            Odometer odometer = (Odometer) measurement;
            final double km = odometer.getValue().doubleValue();
            // log every 200 meters
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final FuelLevel fuelLevel = (FuelLevel) mVehicleManager.get(FuelLevel.class);
                        final double fuelLeveld = fuelLevel.getValue().doubleValue();

                        if (km - lastMeasurement > 0.1) {
                            Map<String, Double> m = new HashMap<String, Double>();
                            m.put("fuel", fuelLeveld);
                            m.put("km", km);
                            logData("fuel-km", new Gson().toJson(m));
                            lastMeasurement = km;
                        }

                        mFuelPercentageInfoTextView.setText((int) fuelLeveld + "%");
                    } catch (UnrecognizedMeasurementTypeException e) {
                        e.printStackTrace();
                    } catch (NoValueException e) {
                        e.printStackTrace();
                    }
                }
            });

            RestInterfaceController controller = Controller.getController();
            Call<Double> call = controller.refuelTime();
            call.enqueue(new Callback<Double>() {
                @Override
                public void onResponse(Call<Double> call, Response<Double> response) {
                    final double f = response.body();
                    Log.e("w", "" + f);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFuelOutageInfoTextView.setText(String.format("%.1f km", f));
                        }
                    });
                }

                @Override
                public void onFailure(Call<Double> call, Throwable t) {

                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");

            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            mVehicleManager.addListener(Latitude.class, mLatitudeListener);
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    public HomeTabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeTabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeTabFragment newInstance() {
        HomeTabFragment fragment = new HomeTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, "wow");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLocationInfoTextView = (TextView) getView().findViewById(R.id.tv_location_info);
        mSpeedInfoTextView = (TextView) getView().findViewById(R.id.tv_speed_info);
        mSpeedLimitInfoTextView = (TextView) getView().findViewById(R.id.tv_speed_limit_info);
        mFuelPercentageInfoTextView = (TextView) getView().findViewById(R.id.tv_fuel_percentage_info);
        mFuelOutageInfoTextView = (TextView) getView().findViewById(R.id.tv_fuel_outage_info);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mVehicleManager == null) {
            Intent intent = new Intent(getActivity(), VehicleManager.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");

            mVehicleManager.removeListener(Latitude.class, mLatitudeListener);
            mVehicleManager.removeListener(VehicleSpeed.class, mSpeedListener);
            mVehicleManager.removeListener(Odometer.class, mOdometerListener);

            getActivity().unbindService(mConnection);

            mVehicleManager = null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
