package cn.studyjams.s2.sj0211.freetalk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TextWatcher{

    private long id;
    private String name;
    private boolean enterFlag = false;

    private String nickName;

    private CardView cv_search, cv_info, cv_create, cv_wait;

    private EditText et_search;

    private List<String> roomIds;
    private List<Map<String,Object>> rooms;
//    private ListView listView;
    private SwipeMenuListView listView;
    private SimpleAdapter listAdapter;

    private SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF4,
                    0x43, 0x36)));
            deleteItem.setWidth(dp2px(72));
            deleteItem.setTitle("删除");
            deleteItem.setTitleSize(18);
            deleteItem.setTitleColor(Color.WHITE);
            menu.addMenuItem(deleteItem);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(enterFlag) {
//                    Snackbar.make(view,"正在进入房间或创建房间，请稍后再试！",Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                enterFlag = true;
//                inputTitleDialog();
                id = 0;
                name = "LocalRoom";
                enterChatRoom();
            }
        });
        cv_search = (CardView) findViewById(R.id.cv_search);
        cv_info = (CardView) findViewById(R.id.cv_info);
        cv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSearch();
                id = Long.valueOf(((TextView) view.findViewById(R.id.tv_roomid)).getText().toString());
                name = ((TextView) view.findViewById(R.id.tv_roomname)).getText().toString();
                enterChatRoom();
            }
        });
        cv_create = (CardView) findViewById(R.id.cv_create);
        cv_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSearch();
                inputRoomNameDialog();
            }
        });
        cv_wait = (CardView) findViewById(R.id.cv_wait);
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.addTextChangedListener(this);
        findViewById(R.id.iv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSearch();
            }
        });

        getNickName();
        setTitle("Free Talk("+nickName+")");
        roomIds = new ArrayList<>();
        rooms = new ArrayList<>();
        loadRooms();
//
        listView = (SwipeMenuListView) findViewById(R.id.lv_room);
        listAdapter = new SimpleAdapter(this, rooms, R.layout.content_room, new String[]{"roomid","roomname"}, new int[]{R.id.tv_roomid, R.id.tv_roomname});
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                id = Long.valueOf(((TextView) view.findViewById(R.id.tv_roomid)).getText().toString());
                name = ((TextView) view.findViewById(R.id.tv_roomname)).getText().toString();
                enterChatRoom();
            }
        });

        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                deleteRoom(position);
                return false;
            }
        });
    }

//    private void inputTitleDialog() {
//
//        final EditText inputServer = new EditText(this);
//        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
//        inputServer.setFocusable(true);
//        inputServer.requestFocus();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("输入RoomId：").setView(inputServer).setNegativeButton(
//                "取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        enterFlag = false;
//                    }
//                });
//        builder.setPositiveButton("进入",
//                new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int which) {
//                        String inputName = inputServer.getText().toString();
//                        id = inputName.length() > 0 ? Long.valueOf(inputName) : 0;
//                        judgeRoom();
//                    }
//                });
//        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                enterFlag = false;
//            }
//        });
//        builder.show();
//    }

    private void inputRoomNameDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入RoomName：").setView(inputServer).setNegativeButton(
                "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        enterFlag = false;
                    }
                });
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        final String inputName = inputServer.getText().toString();
                        if (inputName.length() <= 0) return;
                        FirebaseDatabase.getInstance().getReference("Rooms/"+id).setValue(ChatRoom.toValue(id,inputName))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                name = inputName;
                                enterChatRoom();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                enterFlag = false;
                                Snackbar.make(findViewById(R.id.fab),"创建失败！",Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                enterFlag = false;
            }
        });
        builder.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                inputServer.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (!imm.isActive(et_search)) imm.toggleSoftInput(0, 0);//InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
    }

    private void judgeRoom() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rooms/"+id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.child("RoomName").getValue(String.class);
                    enterChatRoom();
                } else {
                    enterFlag = false;
                    Snackbar.make(findViewById(R.id.fab),"聊天室不存在！是否创建？",Snackbar.LENGTH_INDEFINITE).setAction("创建", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enterFlag = true;
                            inputRoomNameDialog();
                        }
                    }).show();
                }
                ref.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                enterFlag = false;
            }
        });
    }

    private void enterChatRoom() {
        enterFlag = false;
        if (!roomIds.contains(""+id))
            addRoom();
        Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
        intent.putExtra("RoomId",id);
        intent.putExtra("RoomName",name);
        intent.putExtra("NickName", nickName);
        startActivity(intent);
    }

    private void getNickName() {
        SharedPreferences sp  = getSharedPreferences("info", Context.MODE_PRIVATE);
        nickName = sp.getString("nickname", "null");
    }

    private void saveNickName() {
        SharedPreferences sp = getSharedPreferences("info", MODE_PRIVATE);
        sp.edit().putString("nickname", nickName).commit();
    }

    private void deleteRoom(int position) {
        String roomId = roomIds.remove(position);
        rooms.remove(position);
        SharedPreferences sp = getSharedPreferences("rooms", MODE_PRIVATE);
        sp.edit().remove(roomId).commit();
        listAdapter.notifyDataSetChanged();
    }

    private void addRoom() {
        Map<String, Object> room = new HashMap<>(2);
        room.put("roomid", id+"");
        room.put("roomname",  name);
        rooms.add(room);
        roomIds.add(id+"");
        SharedPreferences sp = getSharedPreferences("rooms", MODE_PRIVATE);
        sp.edit().putString(""+id,name).commit();
        listAdapter.notifyDataSetChanged();
    }

    private void loadRooms() {
        SharedPreferences sp = getSharedPreferences("rooms", MODE_PRIVATE);
        Map<String, ?> roomData = sp.getAll();
        for (String key : roomData.keySet()) {
            Map<String, Object> room = new HashMap<>(2);
            room.put("roomid", key);
            room.put("roomname",  (String)roomData.get(key));
            roomIds.add(key);
            rooms.add(room);
        }
    }

    private void inputNameDialog() {
        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);
        inputServer.setText(nickName);
        inputServer.setSelection(nickName.length());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入昵称：").setView(inputServer).setNegativeButton(
                "取消", null);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        final String inputName = inputServer.getText().toString();
                        if (inputName.length() <= 0) return;
                        nickName = inputName;
                        setTitle("Free Talk("+nickName+")");
                        saveNickName();
                    }
                });
        builder.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                inputServer.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (!imm.isActive(et_search)) imm.toggleSoftInput(0, 0);//InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,Menu.NONE,0,"设置昵称");
        menu.add(Menu.NONE,Menu.NONE,1,"搜索房间");
        menu.add(Menu.NONE,Menu.NONE,2,"退出");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            case 0:
                inputNameDialog();
                break;
            case 1:
                cv_search.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                et_search.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (!imm.isActive(et_search)) imm.toggleSoftInput(0, 0);//InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case 2:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        FirebaseDatabase.getInstance().getReference("Rooms/"+id).removeEventListener(searchListener);
        String text = editable.toString();
        cv_wait.setVisibility(View.GONE);
        cv_create.setVisibility(View.GONE);
        cv_info.setVisibility(View.GONE);
        if (text.length() <= 0)  {
            findViewById(R.id.iv_clear).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.iv_clear).setVisibility(View.VISIBLE);
        id = Long.valueOf(text);
        cv_wait.setVisibility(View.VISIBLE);
        searchRoom();
    }

    private ValueEventListener searchListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                name = dataSnapshot.child("RoomName").getValue(String.class);
                ((TextView)cv_info.findViewById(R.id.tv_roomid)).setText(id+"");
                ((TextView)cv_info.findViewById(R.id.tv_roomname)).setText(name);
                cv_wait.setVisibility(View.GONE);
                cv_info.setVisibility(View.VISIBLE);
            } else {
                cv_wait.setVisibility(View.GONE);
                cv_create.setVisibility(View.VISIBLE);
            }
            dataSnapshot.getRef().removeEventListener(this);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(cv_wait,"DatabaseError:"+databaseError.toString(),Snackbar.LENGTH_SHORT).show();
            searchRoom();
        }
    };

    private void searchRoom() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rooms/"+id);
        ref.addValueEventListener(searchListener);
    }

    private void stopSearch() {
        et_search.setText("");
        cv_search.clearFocus();
        cv_search.setVisibility(View.GONE);
        cv_wait.setVisibility(View.GONE);
        cv_create.setVisibility(View.GONE);
        cv_info.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }
}