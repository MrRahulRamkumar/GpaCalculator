package rahulramkumar.com.gpacalculator;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rahulramkumar.com.gpacalculator.MainActivity;

/**
 * FFEE5A6D
 * Created by Rahul Ramkumar on 25-03-2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayAdapter<String> coursesAdapter;
    private ArrayAdapter<String> gradeLettersAdapter;
    private int count = 4;

    private List<String> courseInputList;
    private List<String> gradeInputList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AutoCompleteTextView courseInput;
        public Spinner gradeSpinner;

        public ViewHolder(View view) {
            super(view);

            courseInput = view.findViewById(R.id.course_input);
            gradeSpinner = view.findViewById(R.id.grade_spinner);

            courseInputList = new ArrayList<>();
            gradeInputList = new ArrayList<>();
        }
    }

    public RecyclerAdapter(ArrayAdapter<String> coursesAdapter, ArrayAdapter<String> gradeLettersAdapter) {

        this.coursesAdapter = coursesAdapter;
        this.gradeLettersAdapter = gradeLettersAdapter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_course_input, parent, false);

        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.courseInput.setThreshold(1);
        holder.courseInput.setAdapter(coursesAdapter);
        holder.courseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                holder.courseInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override

                    public void onFocusChange(View v, boolean hasFocus) {
                        if(search(holder.courseInput.getText().toString(),courseInputList) == -1) {
                            gradeInputList.add(holder.gradeSpinner.getSelectedItem().toString());
                            courseInputList.add(holder.courseInput.getText().toString());
                        }
                    }
                });
            }
        });

        holder.gradeSpinner.setAdapter(gradeLettersAdapter);
        holder.gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = search(holder.courseInput.getText().toString(),courseInputList);
                if(pos >= 0 && !gradeInputList.get(pos).equals(holder.gradeSpinner.getSelectedItem().toString())) {
                    gradeInputList.set(pos,holder.gradeSpinner.getSelectedItem().toString());
                }
                else if(pos == -1) {
                    gradeInputList.add(holder.gradeSpinner.getSelectedItem().toString());
                    courseInputList.add(holder.courseInput.getText().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void addItem() {
        count++;
    }

    public String getInputedCourse(int index) {
        return courseInputList.get(index);
    }

    public String getInputedGrade(int index) {
        return gradeInputList.get(index);
    }

    public void deleteInputedGradeAndCourse(int index) {
        courseInputList.remove(index);
        gradeInputList.remove(index);
    }

    public int getInputSize() {
        return courseInputList.size();
    }

    private int search(String searchStr,List<String> aList) {

        Iterator<String> iterator = aList.iterator();
        String curItem = "";
        int pos = 0;

        while (iterator.hasNext()) {
            curItem = iterator.next();
            if (curItem.equals(searchStr)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }
}


