package sg.edu.rp.id19037610.p09_gettingmylocation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class CheckRecords extends AppCompatActivity {

    ListView lvLocations;
    ArrayList<String> alLocations;
    Button btnRefresh;
    TextView tvCount;
    int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_records);

        lvLocations = findViewById(R.id.lvLocations);
        alLocations = new ArrayList<String>();
        btnRefresh = findViewById(R.id.btnRefresh);
        tvCount = findViewById(R.id.tvCount);

        tvCount.setText(String.format("Number of records: %d", num));

        loadLocations();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alLocations.clear();
                loadLocations();
            }
        });
    }

    public void loadLocations(){
        String folderLocation = getFilesDir().getAbsolutePath() + "/MyLocations";
        File targetFile = new File(folderLocation, "locations.txt");

        if (targetFile.exists()){
            try {
                FileReader reader = new FileReader(targetFile);
                BufferedReader br = new BufferedReader(reader);

                String line = br.readLine();

                while (line != null){
                    alLocations.add(line);
                    line = br.readLine();
                }
                br.close();
                reader.close();

                ArrayAdapter aa = new ArrayAdapter(CheckRecords.this, android.R.layout.simple_list_item_1, alLocations);
                lvLocations.setAdapter(aa);
                tvCount.setText(String.format("Number of records: %d", alLocations.size()));

            }catch (Exception e){
                Toast.makeText(CheckRecords.this, "Failed to read", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}