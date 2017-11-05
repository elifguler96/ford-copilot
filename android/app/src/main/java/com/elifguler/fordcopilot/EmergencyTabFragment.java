package com.elifguler.fordcopilot;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EmergencyTabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EmergencyTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmergencyTabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText mNumberEditText;
    private EditText mMessageEditText;
    private Button mSaveButton;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EmergencyTabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmergencyTabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EmergencyTabFragment newInstance() {
        EmergencyTabFragment fragment = new EmergencyTabFragment();
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
        return inflater.inflate(R.layout.fragment_emergency_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNumberEditText = (EditText) getActivity().findViewById(R.id.number);
        mMessageEditText = (EditText) getActivity().findViewById(R.id.message);
        mSaveButton = (Button) getActivity().findViewById(R.id.save);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String number = mNumberEditText.getText().toString();
                String message = mMessageEditText.getText().toString();

                Emergency emergency = new Emergency();
                emergency.number = number;
                emergency.message = message;

                RestInterfaceController controller = Controller.getController();
                Call<Emergency> call = controller.emergency(emergency);
                call.enqueue(new Callback<Emergency>() {
                    @Override
                    public void onResponse(Call<Emergency> call, Response<Emergency> response) {
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onFailure(Call<Emergency> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT);
                    }
                });
            }
        });
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
