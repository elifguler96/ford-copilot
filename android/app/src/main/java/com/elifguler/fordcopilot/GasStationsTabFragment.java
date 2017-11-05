package com.elifguler.fordcopilot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GasStationsTabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GasStationsTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GasStationsTabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FrameLayout gs1Warning;
    private TextView gs1NameTextView;
    private TextView gs1PriceTextView;
    private TextView gs1DistanceTextView;
    private TextView gs1LasteRefuelTextView;
    private TextView gs1FuelEfficiencyTextView;

    private FrameLayout gs2Warning;
    private TextView gs2NameTextView;
    private TextView gs2PriceTextView;
    private TextView gs2DistanceTextView;
    private TextView gs2LasteRefuelTextView;
    private TextView gs2FuelEfficiencyTextView;

    private FrameLayout gs3Warning;
    private TextView gs3NameTextView;
    private TextView gs3PriceTextView;
    private TextView gs3DistanceTextView;
    private TextView gs3LasteRefuelTextView;
    private TextView gs3FuelEfficiencyTextView;

    private static final String TAG = "HomeTabFragment";
    private VehicleManager mVehicleManager;

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = 0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance) / 1000;
    }


    private Latitude.Listener mLatitudeListener = new Latitude.Listener() {
        int dataCount = 0;

        @Override
        public void receive(Measurement measurement) {
            final Latitude lat = (Latitude) measurement;
            try {
                Longitude lng = (Longitude) mVehicleManager.get(Longitude.class);
                final double latd = lat.getValue().doubleValue();
                final double lngd = lng.getValue().doubleValue();

                if (dataCount++ % 10 != 0) return;
                RestInterfaceController controller = Controller.getController();
                Call<List<GasStation>> call = controller.gasStations(new HashMap<String, Double>(){{
                    put("lat", latd);
                    put("lng", lngd);
                }});

                call.enqueue(new Callback<List<GasStation>>() {
                    @Override
                    public void onResponse(Call<List<GasStation>> call, Response<List<GasStation>> response) {
                        GasStation gs1 = null;
                        GasStation gs2 = null;
                        GasStation gs3 = null;

                        if (!response.body().isEmpty()) {
                            gs1 = response.body().get(0);
                            if (response.body().size() > 0) {
                                gs2 = response.body().get(1);
                                if (response.body().size() > 1) {
                                    gs3 = response.body().get(2);
                                    if (gs3.fuelEfficiency > gs2.fuelEfficiency &&
                                            gs3.fuelEfficiency > gs1.fuelEfficiency) {
                                        gs3Warning.setBackgroundColor(Color.GREEN);
                                        gs3Warning.setVisibility(View.VISIBLE);

                                        if (gs2.fuelEfficiency > gs1.fuelEfficiency) {
                                            gs1Warning.setBackgroundColor(Color.RED);
                                            gs1Warning.setVisibility(View.VISIBLE);
                                            gs2Warning.setVisibility(View.INVISIBLE);
                                        } else {
                                            gs2Warning.setBackgroundColor(Color.RED);
                                            gs2Warning.setVisibility(View.VISIBLE);
                                            gs1Warning.setVisibility(View.INVISIBLE);
                                        }
                                    } else if (gs2.fuelEfficiency > gs3.fuelEfficiency &&
                                            gs2.fuelEfficiency > gs1.fuelEfficiency) {
                                        gs2Warning.setBackgroundColor(Color.GREEN);
                                        gs2Warning.setVisibility(View.VISIBLE);

                                        if (gs3.fuelEfficiency > gs1.fuelEfficiency) {
                                            gs1Warning.setBackgroundColor(Color.RED);
                                            gs1Warning.setVisibility(View.VISIBLE);
                                            gs3Warning.setVisibility(View.INVISIBLE);
                                        } else {
                                            gs3Warning.setBackgroundColor(Color.RED);
                                            gs3Warning.setVisibility(View.VISIBLE);
                                            gs1Warning.setVisibility(View.INVISIBLE);
                                        }
                                    } else {
                                        gs1Warning.setBackgroundColor(Color.GREEN);
                                        gs1Warning.setVisibility(View.VISIBLE);

                                        if (gs3.fuelEfficiency > gs2.fuelEfficiency) {
                                            gs2Warning.setBackgroundColor(Color.RED);
                                            gs2Warning.setVisibility(View.VISIBLE);
                                            gs3Warning.setVisibility(View.INVISIBLE);
                                        } else {
                                            gs3Warning.setBackgroundColor(Color.RED);
                                            gs3Warning.setVisibility(View.VISIBLE);
                                            gs2Warning.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                }
                            }
                        }

                        if (gs1 != null) {
                            gs1NameTextView.setText(gs1.name);
                            gs1PriceTextView.setText("" + gs1.price);
                            double d = distance(latd, gs1.location.latitude, lngd,
                                    gs1.location
                                    .longitude);
                            gs1DistanceTextView.setText(String.format("%.1f km", d));
                            gs1FuelEfficiencyTextView.setText("" + gs1.fuelEfficiency + " km/lt");
                            gs1LasteRefuelTextView.setText(gs1.lastRefuel);
                        }

                        if (gs2 != null) {
                            gs2NameTextView.setText(gs2.name);
                            gs2PriceTextView.setText("" + gs2.price);
                            double d = distance(latd, gs2.location.latitude, lngd, gs2.location
                                    .longitude);
                            gs2DistanceTextView.setText(String.format("%.1f km", d));
                            gs2FuelEfficiencyTextView.setText("" + gs2.fuelEfficiency + " km/lt");
                            gs2LasteRefuelTextView.setText(gs2.lastRefuel);
                        }


                        if (gs3 != null) {
                            gs3NameTextView.setText(gs3.name);
                            gs3PriceTextView.setText("" + gs3.price);
                            double d = distance(latd, gs3.location.latitude, lngd, gs3.location
                                    .longitude);
                            gs3DistanceTextView.setText(String.format("%.1f km", d));
                            gs3FuelEfficiencyTextView.setText("" + gs3.fuelEfficiency + " km/lt");
                            gs3LasteRefuelTextView.setText(gs3.lastRefuel);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GasStation>> call, Throwable t) {

                    }
                });
            } catch(NoValueException e) {
                Log.w(TAG, "The vehicle may not have made the measurement yet");
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "The measurement type was not recognized");
            }

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");

            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            mVehicleManager.addListener(Latitude.class, mLatitudeListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service disconnected unexpectedly");
            mVehicleManager = null;
        }
    };


    public GasStationsTabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GasStationsTabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GasStationsTabFragment newInstance() {
        GasStationsTabFragment fragment = new GasStationsTabFragment();
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
        return inflater.inflate(R.layout.fragment_gas_stations_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gs1Warning = (FrameLayout) getActivity().findViewById(R.id.warn_gs1);
        gs1NameTextView = (TextView) getActivity().findViewById(R.id.tv_gs1_name);
        gs1DistanceTextView = (TextView) getActivity().findViewById(R.id.tv_gs1_distance);
        gs1LasteRefuelTextView = (TextView) getActivity().findViewById(R.id.tv_gs1_refuel);
        gs1FuelEfficiencyTextView = (TextView) getActivity().findViewById(R.id.tv_gs1_efficiency);
        gs1PriceTextView = (TextView) getActivity().findViewById(R.id.tv_gs1_price);

        gs2Warning = (FrameLayout) getActivity().findViewById(R.id.warn_gs2);
        gs2NameTextView = (TextView) getActivity().findViewById(R.id.tv_gs2_name);
        gs2DistanceTextView = (TextView) getActivity().findViewById(R.id.tv_gs2_distance);
        gs2LasteRefuelTextView = (TextView) getActivity().findViewById(R.id.tv_gs2_refuel);
        gs2FuelEfficiencyTextView = (TextView) getActivity().findViewById(R.id.tv_gs2_efficiency);
        gs2PriceTextView = (TextView) getActivity().findViewById(R.id.tv_gs2_price);

        gs3Warning = (FrameLayout) getActivity().findViewById(R.id.warn_gs3);
        gs3NameTextView = (TextView) getActivity().findViewById(R.id.tv_gs3_name);
        gs3DistanceTextView = (TextView) getActivity().findViewById(R.id.tv_gs3_distance);
        gs3LasteRefuelTextView = (TextView) getActivity().findViewById(R.id.tv_gs3_refuel);
        gs3FuelEfficiencyTextView = (TextView) getActivity().findViewById(R.id.tv_gs3_efficiency);
        gs3PriceTextView = (TextView) getActivity().findViewById(R.id.tv_gs3_price);

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
