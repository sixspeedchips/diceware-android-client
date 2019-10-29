package edu.cnm.deepdive.diceware.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter;
import edu.cnm.deepdive.diceware.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {


  private MainViewModel viewModel;
  private RecyclerView passphraseList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ProgressBar waiting = findViewById(R.id.waiting);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
    passphraseList = findViewById(R.id.keyword_list);
    viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    GoogleSignInService.getInstance().getAccount().observe(this, (account -> {
      viewModel.setAccount(account);
    }));
    viewModel.getPassphrases().observe(this, (passphrases) -> {

      PassphraseAdapter adapter = new PassphraseAdapter(this, passphrases,
          (view, position, passphrase) -> {
            Log.d("Passphrase click", passphrase.getKey());
          },
          ((menu, position, passphrase) -> {
            Log.d("Long press", passphrase.getKey());
            getMenuInflater().inflate(R.menu.passphrase_context, menu);
            menu.findItem(R.id.delete_passphrase).setOnMenuItemClickListener((menuItem) -> {
              waiting.setVisibility(View.VISIBLE);
              Log.d("Delete selected", passphrase.getKey());
              viewModel.deletePassphrase(passphrase);
              return true;
            });
          }));
      passphraseList.setAdapter(adapter);
      waiting.setVisibility(View.GONE);
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
