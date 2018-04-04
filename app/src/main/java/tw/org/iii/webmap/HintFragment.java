package tw.org.iii.webmap;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class HintFragment extends Fragment {
    private TextView hints;

    public HintFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hint, container, false);
        hints = view.findViewById(R.id.hints);
        return view;
    }
    public void writeHints(String hint){
        hints.append(hint);
    }
    public void clearText(){
        hints.setText("");
    }
}
