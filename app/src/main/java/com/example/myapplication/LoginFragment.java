package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;

    public LoginFragment() {}

    // פונקציית Lifecycle האחראית על יצירת הממשק הוויזואלי.
    // היא מבצעת Inflation לקובץ ה-XML והופכת אותו לאובייקט View בר-קיימא בזיכרון.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    // פונקציה המופעלת מיד לאחר שה-View נוצר. כאן מתבצע קישור הרכיבים (Binding) והגדרת הלוגיקה של הכפתורים.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול רכיב ה-Toggle המאפשר בחירה בין מצב "אמן" לבין מצב "בליין".
        MaterialButtonToggleGroup toggleUserType = view.findViewById(R.id.toggleUserType);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        // קישור ל-TextInputLayouts כדי שנוכל להציג שגיאות ויזואליות (Error States) מתחת לשדות.
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);

        Button btnAction = view.findViewById(R.id.btnAction);
        TextView tvSwitchMode = view.findViewById(R.id.tvSwitchMode);

        // מאזין ללחיצה על כפתור ההתחברות. הפונקציה מבצעת וולידציה ראשונית לפני שליחה לשרת.
        btnAction.setOnClickListener(v -> {
            // בדיקה איזה כפתור נבחר ב-ToggleGroup.
            int checkedId = toggleUserType.getCheckedButtonId();

            // אם לא נבחר סוג משתמש, נעצור את התהליך ונציג התראה.
            if (checkedId == View.NO_ID) {
                Toast.makeText(getContext(), "נא לבחור סוג משתמש (בליין או אמן)", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // איפוס שגיאות קודמות מה-UI (Clean State) לפני ביצוע בדיקה חדשה.
            if (tilEmail != null) tilEmail.setError(null);
            if (tilPassword != null) tilPassword.setError(null);

            boolean isValid = true;

            // בדיקת תקינות: שדה אימייל ריק.
            if (email.isEmpty()) {
                if (tilEmail != null) tilEmail.setError("חובה להזין אימייל");
                isValid = false;
            }

            // בדיקת תקינות: שדה סיסמה ריק.
            if (password.isEmpty()) {
                if (tilPassword != null) tilPassword.setError("חובה להזין סיסמה");
                isValid = false;
            }

            // אם אחת הבדיקות נכשלה, לא נמשיך לביצוע הלוגין.
            if (!isValid) return;

            // קביעת סוג המשתמש לצורך לוגיקת הניווט וההרשאות בהמשך.
            boolean isArtist = (checkedId == R.id.btnTypeArtist);

            // שימוש ב-Delegation Pattern: העברת פעולת הלוגין ל-MainActivity.
            // אנו בודקים בעזרת instanceof שה-Activity המארח הוא אכן MainActivity כדי למנוע שגיאות Runtime.
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).login(email, password, isArtist);
            }
        });

        // מאזין ללחיצה על טקסט המעבר להרשמה.
        tvSwitchMode.setOnClickListener(v -> {
            int checkedId = toggleUserType.getCheckedButtonId();

            // ניווט דינמי: הכתובת להרשמה משתנה לפי בחירת סוג המשתמש ב-Toggle.
            if (checkedId == R.id.btnTypeUser) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerUserFragment);
            }
            else if (checkedId == R.id.btnTypeArtist) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerArtistFragment);
            }
            else {
                // חוויית משתמש (UX): דורשים מהמשתמש לבחור סוג לפני המעבר למסך ההרשמה המתאים.
                Toast.makeText(getContext(), "נא לבחור קודם בליין 🎉 או אמן 🎤", Toast.LENGTH_LONG).show();
            }
        });
    }

    // פונקציית Lifecycle המופעלת בכל פעם שהפרגמנט חוזר לקדמת הבמה.
    // אנו מנקים את השדות (Security Best Practice) כדי למנוע השארת פרטים רגישים על המסך.
    @Override
    public void onResume() {
        super.onResume();
        if (etEmail != null) etEmail.setText("");
        if (etPassword != null) etPassword.setText("");
    }
}