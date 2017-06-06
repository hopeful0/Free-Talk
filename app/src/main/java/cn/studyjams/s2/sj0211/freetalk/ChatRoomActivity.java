package cn.studyjams.s2.sj0211.freetalk;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatRoomActivity extends AppCompatActivity implements ChildEventListener{

    private long roomId;

    private DatabaseReference ref;

    private ListView listView;
    private SimpleAdapter listAdapter;

    private List<Map<String,Object>> list;
    private List<Integer> colorList;
    private int lh = 0;

    private EditText editText;

    private Button button;

    private String name;

    private Location location;

    private boolean locationFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle bundle = getIntent().getExtras();
        name = bundle.getString("NickName");
        setTitle(bundle.getString("RoomName")+"("+name+")");
        Random random = new Random();
        for (int i = 0; i< 3; i ++)
            name += String.format("%02x", random.nextInt(256));

        roomId = bundle.getLong("RoomId");
        ref = FirebaseDatabase.getInstance().getReference("Messages/"+roomId);
        ref.addChildEventListener(this);

        list = new ArrayList<>(100);
        colorList = new ArrayList<>(100);
        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new SimpleAdapter(this,list,R.layout.message_item,new String[]{"title","message"},new int[]{R.id.textView2,R.id.textView3}) {
            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((ImageView)v.findViewById(R.id.imageView2)).setColorFilter(colorList.get(position), PorterDuff.Mode.SRC_IN);
                return v;
            }
        };
        listView.setAdapter(listAdapter);
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (lh == 0) lh = listView.getHeight();
                if (lh != listView.getHeight()) {
                    if (!(lh < listView.getHeight() && listView.getCount() > 0 && listView.getLastVisiblePosition() == listView.getCount() - 1 && listView.getChildAt(listView.getChildCount()-1).getBottom() == listView.getBottom())) {
                        listView.smoothScrollBy(lh - listView.getHeight() , 0);
                    }
                    lh = listView.getHeight();

                }
            }
        });

        button = (Button) findViewById(R.id.button);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                button.setEnabled(editable.toString().length() > 0);
                if (editable.length() > 128)
                    editable.delete(128, editable.length());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();
                if (msg.length() <= 0) return;
                if (roomId == 0) {
                    if (locationFinished) {
                        ref.push().setValue(Message.toValue(name, msg, location));
                    } else {
                        Snackbar.make(button,"未定位！",Snackbar.LENGTH_INDEFINITE).setAction("退出", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                                locationFinished = true;
                            }
                        }).show();
                    }
                } else {
                    ref.push().setValue(Message.toValue(name, msg));
                }
                editText.setText("");
            }
        });

        if (roomId == 0) {
            Location.getLocation(this, new Location.OnLocationFinished() {
                @Override
                public void onFinished(Location location) {
                    if (locationFinished) return;
                    if (location == null) {
                        Toast.makeText(ChatRoomActivity.this, "定位失败！", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ChatRoomActivity.this.location = location;
                    locationFinished = true;
                }
            });
        }
    }

    @Override

    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot == null || dataSnapshot.child("TimeStamp").getValue(Long.class) <= System.currentTimeMillis() - 1000 * 60) return;
        if (roomId == 0) {
            if (!locationFinished) return;
            Location l2 = new Location(dataSnapshot.child("Location"));
            //if (location.getDistance(l2) > 5) return;
        }
        Message message = new Message(dataSnapshot);
        Map<String,Object> map = new HashMap<>(2);
        String title = message.fromName + "(" + message.getTime() + "):";
        if (roomId == 0) title += String.format("%.3fkm",location.getDistance(message.location));
        map.put("title",title);
        map.put("message",message.message);
        if (list.size() > 99) {
            list.remove(0);
            colorList.remove(0);
        }
        list.add(map);
        colorList.add(message.color);
        listAdapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(listAdapter.getCount() - 1);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
