package edu.kit.tm.pseprak2.alushare.view;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.presenter.PersonalInfoPresenter;


public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);



    }

    @Override
    protected  void onStart(){
        super.onStart();
        PersonalInfoPresenter presenter = new PersonalInfoPresenter();
        presenter.onTakeView(this);
        presenter.setQRCode();
        presenter.setTorId();
        initToolbar(presenter.getName());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case (R.id.action_add):
                startActivity(new Intent(this, CreateContactActivity.class));
                return true;
            case (R.id.action_settings):
                startActivity(new Intent(this, Preferences.class));
                return true;
            case (android.R.id.home):
                this.finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
        System.gc();
    }

    private void initToolbar(String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Kontaktinformation");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        Toolbar subToolbar = (Toolbar) findViewById(R.id.subtoolbar);
        subToolbar.setSubtitle(name);
        subToolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


}
