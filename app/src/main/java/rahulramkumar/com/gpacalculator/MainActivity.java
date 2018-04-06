package rahulramkumar.com.gpacalculator;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    public static boolean calculatePressed = false;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<String> courses;
    private String[] gradeLetters = {"-","S","A","B","C","D","E"};
    private HashMap<String, Long> courseDetails;
    private HashMap<String, String> gradeValues;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private Button calculateButton;
    private FloatingActionButton addButton;
    private TextView gpaText,courseText,gradeText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        if (mCurrentUser == null) {
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mCurrentUser = mAuth.getCurrentUser();
                    } else {

                    }
                }
            });

        }

        courseDetails = new HashMap<>();
        mRootRef.child("course_details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                courseDetails = (HashMap<String, Long>) dataSnapshot.getValue();
                initLayout();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gradeValues = new HashMap<>();
        gradeValues.put("S", "10");
        gradeValues.put("A", "9");
        gradeValues.put("B", "8");
        gradeValues.put("C", "7");
        gradeValues.put("D", "6");
        gradeValues.put("E", "5");

        courseText = findViewById(R.id.course_text);
        courseText.setVisibility(View.INVISIBLE);
        gradeText = findViewById(R.id.grade_text);
        gradeText.setVisibility(View.INVISIBLE);
        gpaText = findViewById(R.id.gpa_text);
        gpaText.setVisibility(View.INVISIBLE);
        calculateButton = findViewById(R.id.calculate_button);
        addButton = findViewById(R.id.add_button);
        addButton.setVisibility(View.INVISIBLE);
        calculateButton.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vit AP GPA Calculator");

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePressed = true;
                calculateGPA();
                hideKeyBoard();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerAdapter.addItem();
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.about_button) {
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void initLayout() {
        courses = new ArrayList<>(courseDetails.keySet());

        ArrayAdapter<String> coursesAdapter = new ArrayAdapter<String>(this, R.layout.dropdown, courses);
        ArrayAdapter<String> gradeLettersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, gradeLetters);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerAdapter = new RecyclerAdapter(coursesAdapter,gradeLettersAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setItemViewCacheSize(20);

        gpaText.setVisibility(View.VISIBLE);
        calculateButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        courseText.setVisibility(View.VISIBLE);
        gradeText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void calculateGPA() {
        long sum = 0, totalCredits = 0, gradeValue, credits = 0;
        float finalGPA = 0f;
        boolean canDisplayResult = false;
        int errorCode = 0;

        String inputedGrade,inputedCourse;

        int i = 0;
        for (; i < recyclerAdapter.getInputSize(); i++) {
            inputedCourse = recyclerAdapter.getInputedCourse(i);
            inputedGrade = recyclerAdapter.getInputedGrade(i);

            System.out.println(inputedCourse);
            System.out.println(inputedGrade);

            if(courseDetails.get(inputedCourse) == null && !TextUtils.isEmpty(inputedCourse)) {
                errorCode = 1;
                recyclerAdapter.deleteInputedGradeAndCourse(i);
                canDisplayResult = false;
                break;
            }
            if(courseDetails.get(inputedCourse) == null && TextUtils.isEmpty(inputedCourse) && !inputedGrade.equals("-")) {
                errorCode = 1;
                recyclerAdapter.deleteInputedGradeAndCourse(i);
                canDisplayResult = false;
                break;
            }
            if(courseDetails.get(inputedCourse) != null && !TextUtils.isEmpty(inputedCourse) && inputedGrade.equals("-")) {
                errorCode = 2;
                canDisplayResult = false;
                break;
            }

            if(!TextUtils.isEmpty(inputedCourse) && !inputedGrade.equals("-") && courseDetails.get(inputedCourse) != null) {
                errorCode = -1;
                credits = courseDetails.get(inputedCourse);
                gradeValue = Long.parseLong(gradeValues.get(inputedGrade));
                canDisplayResult = true;

                sum += credits * gradeValue;
                totalCredits += credits;
            }
        }
        if (totalCredits > 0)
            finalGPA = (float) sum / totalCredits;

        if (finalGPA > 0.0 && canDisplayResult) {
            displayAlertMessage(finalGPA);
        }
        else if(errorCode == 1){
            showSnackbar(findViewById(R.id.activity_main), "Invalid course", Snackbar.LENGTH_LONG);
        }
        else if(errorCode == 2) {
            showSnackbar(findViewById(R.id.activity_main), "Invalid Grade", Snackbar.LENGTH_LONG);
        }
        else{
            showSnackbar(findViewById(R.id.activity_main), "Please input the details for at least one course", Snackbar.LENGTH_LONG);
        }

    }
    private void showSnackbar(View view, String message, int duration) {

        final Snackbar snackbar = Snackbar.make(view, message, duration);
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void displayAlertMessage(float gpa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);

        if(gpa >= 7.0) {
            final View view = factory.inflate(R.layout.alert_box_image_clapping, null);
            builder.setView(view);
        }
        else {
            final View view = factory.inflate(R.layout.alert_box_image_crying, null);
            builder.setView(view);
        }

        builder.setTitle("Vit AP GPA Calculator");
        builder.setMessage("Your GPA is " + gpa);
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", null);
        builder.show();
    }
}
