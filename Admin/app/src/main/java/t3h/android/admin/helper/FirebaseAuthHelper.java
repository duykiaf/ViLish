package t3h.android.admin.helper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class FirebaseAuthHelper {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public static void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void forgotPwd(String email, OnCompleteListener<Void> listener) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(listener);
    }

    public static void changePwd(String oldPwd, String newPwd, String confirmNewPwd, OnCompleteListener<Void> listener) {
        FirebaseUser firebaseUser = getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), oldPwd);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
                firebaseUser.updatePassword(newPwd).addOnCompleteListener(listener);
           } else {
                listener.onComplete(task);
           }
        });
    }

    public static void signOut() {
        auth.signOut();
    }
}
