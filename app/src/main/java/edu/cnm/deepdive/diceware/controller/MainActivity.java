package edu.cnm.deepdive.diceware.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.service.DicewareService;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
    RecyclerView passphraseList = findViewById(R.id.keyword_list);
    GoogleSignInService.getInstance().getAccount().observe(this, (account) -> {
      if (account != null) {
        String token = getString(R.string.oauth_header, account.getIdToken());
        Log.d("Oauth2.0 token", token);
        DicewareService.getInstance().getAll(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((passphrases) -> {
              PassphraseAdapter adapter = new PassphraseAdapter(this, passphrases,
                  (view, position, passphrase) -> {
                    Log.d("Passphrase click", passphrase.getKey());
                  },
                  ((menu, position, passphrase) -> {
                    //TODO add code o pop up editor.
                    Log.d("Long press", passphrase.getKey());
                    getMenuInflater().inflate(R.menu.passphrase_context, menu);
                    menu.findItem(R.id.delete_passphrase).setOnMenuItemClickListener((menuItem) -> {
                      Log.d("Delete selected", passphrase.getKey());
                      // TODO send request to server to delete passphrase; refresh view.
                      return true;
                    });
                  }));
              passphraseList.setAdapter(adapter);
            });
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    boolean handled = true;

    switch (id) {
      case R.id.action_settings:
        break;
      case R.id.log_out:
        signOut();

        break;
      default:
        handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  private void signOut() {
    GoogleSignInService.getInstance().signOut()
        .addOnCompleteListener((task) -> {
          Intent intent = new Intent(this, LoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        });
  }
}
