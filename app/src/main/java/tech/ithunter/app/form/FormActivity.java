package tech.ithunter.app.form;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.kofigyan.stateprogressbar.StateProgressBar;

import tech.ithunter.app.R;
import tech.ithunter.app.form.fragments.FormFragment01;

public class FormActivity extends AppCompatActivity {

    String[] descriptionSteps = {"You", "Lang", "Spec", "About"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        try {
            StateProgressBar stateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
            stateProgressBar.setStateDescriptionData(descriptionSteps);
            stateProgressBar.setStateDescriptionTypeface("fonts/Roboto-Light.ttf");
            stateProgressBar.setStateNumberTypeface("fonts/Roboto-Light.ttf");
            stateProgressBar.setStateNumberTextSize(14);

            Fragment fragment = new FormFragment01();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragmentLayout, fragment).commit();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}