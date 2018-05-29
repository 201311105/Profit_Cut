package profitcut.votechain;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.NoSuchPaddingException;

public class VoteParticipationActivity extends AppCompatActivity {
    EditText vote_name_Text;
    SQLiteDatabase db;
    dbHelper dh = new dbHelper(this);
    MyApplication myApp = (MyApplication) getApplication();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = dh.getWritableDatabase();
        setContentView(R.layout.activity_vote_participation);
        vote_name_Text = (EditText) findViewById(R.id.vote_name_Text);
    }

    public void onButtonMenu(View view) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        String vote_name = vote_name_Text.getText().toString();
        myApp.subject = vote_name;
        createTable(myApp.subject);
        new PutDataBase(dh).insertUserInfo(myApp.id, myApp.puk, myApp.subject+"_user_info");
        if(!getData()){
            createGenesis();
        }
        Intent MenuIntent = new Intent(VoteParticipationActivity.this, MenuActivity.class);
        startActivity(MenuIntent);
    }
    private void createGenesis() {
        int index = 1;
        myApp.gh = new genesisblock_header(myApp.ver, index, Calendar.getInstance().getTimeInMillis() / 1000, myApp.deadline);
        myApp.gb = new genesisblock(myApp.vb.hash(myApp.gh.toString()), myApp.subject, myApp.constructor, myApp.candidates ,myApp.gh);
        myApp.chain.add(0, myApp.gb);
    }
    private void openDatabase() {
        String name = "profitcut";

        try {
            db = openOrCreateDatabase(
                    name,
                    Activity.MODE_PRIVATE,
                    null);
        } catch (Exception ex) {
        }
    }


    private void createTable(String subject) {
        createchainTable(subject);
        createtransaction_poolTable(subject);
        createmerkle_treeTable(subject);
        createvotersTable(subject);
        createcandidatesTable(subject);
        createusersTable(subject);
    }

    private void createchainTable(String subject) {
        String name = subject + "_chain";
        db.execSQL("create table if not exists " + name + "("
                + " idx integer not null, "
                + " deadline real not null default '0', "
                + " subject text not null default '-', "
                + " constructor text not null default '-', "
                + " ver text not null, "
                + " time real not null, "
                + " proof integer not null default '0', "
                + " previous_hash text not null default '-', "
                + " merkle_root text not null default '-', "
                + " block_hash text not null, "
                + " primary key(idx));");
    }

    private void createtransaction_poolTable(String subject) {
        String name = subject + "_transaction_pool";
        db.execSQL("create table if not exists " + name + "("
                + " idx integer not null, "
                + " voter text not null, "
                + " candidate text not null, "
                + " primary key(idx));");
    }

    private void createmerkle_treeTable(String subject) {
        String name = subject + "_merkle_tree";
        db.execSQL("create table if not exists " + name + "("
                + " idx integer not null, "
                + " node_idx integer not null, "
                + " transaction_hash text not null, "
                + " primary key(idx));");
    }

    private void createvotersTable(String subject) {
        String name = subject + "_voters";
        db.execSQL("create table if not exists " + name + "("
                + " account text not null, "
                + " primary key(account));");
    }

    private void createcandidatesTable(String subject) {
        String name = subject + "_candidates";
        db.execSQL("create table if not exists " + name + "("
                + " candidate text not null, "
                + " primary key(candidate));");
    }

    private void createusersTable(String subject) {
        String name = subject+ "_user_info";
        db.execSQL("create table if not exists " + name + "("
                + " id text not null, "
                + " pk text not null, "
                + " primary key(id));"
        );
    }

    private boolean getData() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        GetDataBase gd = new GetDataBase(db);
        ArrayList<Object> arr = gd.selectChain(myApp.subject + "_chain");
        if(arr.size() == 0) {
            return true;
        }
        gd.selectCandidates(myApp.subject + "_candidates");
        gd.selectUserInfo();
        gd.selectUsers(myApp.subject + "_users");
        gd.selectVoters(myApp.subject + "_voters");

        return false;
    }
}
