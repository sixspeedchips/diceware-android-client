package edu.cnm.deepdive.diceware.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.controller.PassphraseFragment.OnCompleteListener;
import edu.cnm.deepdive.diceware.model.Passphrase;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter;
import edu.cnm.deepdive.diceware.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity implements OnCompleteListener {


  private MainViewModel viewModel;
  private RecyclerView passphraseList;
  private ProgressBar waiting;
  private GoogleSignInService signInService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupUi();
    setupViewModel();
    setupSignIn();

  }

  private void setupViewModel() {
    viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    viewModel.getPassphrases().observe(this, (passphrases) -> {

      PassphraseAdapter adapter = new PassphraseAdapter(this, passphrases,
          (view, position, passphrase) -> {
            Log.d("Passphrase click", passphrase.getKey());
            PassphraseFragment fragment = PassphraseFragment.newInstance(passphrase);
            fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
          },
          ((menu, position, passphrase) -> {
            Log.d("Long press", passphrase.getKey());
            getMenuInflater().inflate(R.menu.passphrase_context, menu);
            menu.findItem(R.id.delete_passphrase).setOnMenuItemClickListener((menuItem) -> {
              waiting.setVisibility(View.VISIBLE);
              refreshSignIn(() -> {
                viewModel.deletePassphrase(passphrase);
              });
              Log.d("Delete selected", passphrase.getKey());
              return true;
            });
          }));
      passphraseList.setAdapter(adapter);
      waiting.setVisibility(View.GONE);
    });
    viewModel.getThrowable().observe(this, (throwable) -> {
      if (throwable != null) {
        waiting.setVisibility(View.GONE);
        Toast.makeText(this,
            String.format("Connection to server failed: %s", throwable.getMessage()),
            Toast.LENGTH_LONG).show();
      }
    });
  }

  private void setupSignIn() {
    signInService = GoogleSignInService.getInstance();
    signInService.getAccount().observe(this, (account -> {
      viewModel.setAccount(account);
    }));
  }

  private void setupUi() {
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> {
      PassphraseFragment fragment = PassphraseFragment.newInstance();
      fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
    });

    waiting = findViewById(R.id.waiting);
    passphraseList = findViewById(R.id.keyword_list);
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
      case R.id.refresh:
        refreshSignIn(() -> {
          viewModel.refreshPassphrases();
        });
        break;
      default:
        handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  private void signOut() {
    signInService.signOut()
        .addOnCompleteListener((task) -> {
          Intent intent = new Intent(this, LoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        });
  }

  private void refreshSignIn(Runnable runnable) {
    signInService.refresh().addOnFailureListener((e) -> {
      signOut();
    }).addOnSuccessListener(account -> {
      runnable.run();
    });
  }

  @Override
  public void complete(Passphrase passphrase) {
    waiting.setVisibility(View.VISIBLE);
    if (passphrase.getId() == 0) {
      viewModel.addPassphrase(passphrase);
    } else {
      viewModel.updatePassphrase(passphrase);
    }


  }
}
