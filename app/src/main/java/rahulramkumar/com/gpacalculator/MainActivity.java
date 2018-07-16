package rahulramkumar.com.gpacalculator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ListView;

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

import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<String> mCourses;
    private String[] gradeLetters = {"-","S","A","B","C","D","E"};
    private HashMap<String, Long> mCourseDetails;
    private HashMap<String, String> mGradeValues;
    RecyclerAdapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vit AP GPA Calculator");

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

        mCourseDetails = new HashMap<>();
        mRootRef.child("course_details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCourseDetails = (HashMap<String, Long>) dataSnapshot.getValue();
                initLayout(7);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mGradeValues = new HashMap<>();
        mGradeValues .put("S", "10");
        mGradeValues .put("A", "9");
        mGradeValues .put("B", "8");
        mGradeValues .put("C", "7");
        mGradeValues .put("D", "6");
        mGradeValues .put("E", "5");
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
        else if(item.getItemId() == R.id.action_calculate) {
            calculateGPA();
            hideKeyBoard();
        }
        else if (item.getItemId() == R.id.action_select_number_of_rows) {
            showInputNumberOfRowsDialog();
        }
        return true;
    }

    private void initLayout(int itemCount) {
        mCourses = new ArrayList<>(mCourseDetails.keySet());

        ArrayAdapter<String> coursesAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_text_view_dropdown, mCourses);
        ArrayAdapter<String> gradeLettersAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown, gradeLetters);


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mRecyclerAdapter = new RecyclerAdapter(coursesAdapter, gradeLettersAdapter, itemCount);
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);

    }

    private void calculateGPA() {
        HashMap<Integer, String> courseInputs = mRecyclerAdapter.getCourseInputs();
        HashMap<Integer, String> gradeInputs = mRecyclerAdapter.getGradeInputs();
        boolean isError = false;
        float totalCredits = 0.0f;
        float sum = 0.0f;

        for(int i = 0; i<mRecyclerAdapter.getItemCount(); i++) {
            String course = courseInputs.get(i);
            String grade = gradeInputs.get(i);

            boolean temp = mCourses.contains(course);
            if(mCourses.contains(course) && !grade.equals("-")) {
                System.out.println(course + " " + grade);
                float credit = mCourseDetails.get(course);
                float gradeValue = Float.parseFloat(mGradeValues.get(grade));
                sum = sum + (credit * gradeValue);
                totalCredits += credit;
            }
            else if(!TextUtils.isEmpty(course) && grade.equals("-")) {
                isError = true;
                break;
            }
            else if(!TextUtils.isEmpty(course) && !mCourses.contains(course)) {
                isError = true;
                break;
            }
        }
        if(isError) {
            showSnackbar(findViewById(R.id.activity_main), "Please enter a valid grade and course", Snackbar.LENGTH_SHORT);
        }
        else {
            float gpa = sum/totalCredits;
            System.out.println(gpa);
            displayGPA(gpa);
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
    private void displayGPA(float gpa) {
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
    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void showInputNumberOfRowsDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.input_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.edit_text);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.enter_the_number_of_rows)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                int numItems = 0;
                                try {
                                    numItems = Integer.parseInt(userInput.getText().toString());
                                } catch (NumberFormatException e) {
                                    numItems = Integer.MAX_VALUE;
                                }

                                initLayout(numItems);


                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
