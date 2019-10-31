package edu.cnm.deepdive.diceware.controller;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;

public class PassphraseFragment extends DialogFragment {

  private onCompleteListener listener;
  private Passphrase passphrase;

  public static PassphraseFragment newInstance() {
    return newInstance(null);
  }

  public static PassphraseFragment newInstance(Passphrase passphrase) {
    PassphraseFragment fragment = new PassphraseFragment();
    Bundle args = new Bundle();
    if (passphrase != null) {
      args.putSerializable("passphrase", passphrase);
    }

    fragment.setArguments(args);
    return fragment;
  }


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Passphrase passphrase = ((Passphrase) getArguments().getSerializable("passphrase"));
    if (passphrase == null) {
      passphrase = new Passphrase();
    }
    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_passphrase, null);
    EditText passphraseKey = view.findViewById(R.id.passphrase_key);
    Passphrase finalPassphrase = passphrase;
    if (passphrase.getKey() != null) {
      passphraseKey.setText(passphrase.getKey());
    }
    return new AlertDialog
        .Builder(getContext())
        .setTitle("Passphrase Details")
        .setView(view)
        .setNegativeButton("Cancel", (dialog, button) -> {
        })
        .setPositiveButton("Okay", (dialog, button) -> {
          if (listener != null) {
            finalPassphrase.setKey(passphraseKey.getText().toString());
            listener.complete(finalPassphrase);
          }
        })
        .create();

  }

  public void setListener(onCompleteListener listener) {
    this.listener = listener;
  }

  @FunctionalInterface
  public interface onCompleteListener {

    void complete(Passphrase passphrase);

  }


}
