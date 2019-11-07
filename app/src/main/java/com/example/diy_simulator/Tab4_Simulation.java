package com.example.diy_simulator;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tab4_Simulation extends Fragment  {

    private LinearLayout simul_menu_layout;
    private LinearLayout blur;
    private View view;
    Animation animation;

    float oldXvalue;
    float oldYvalue;
    ImageView imageView;
    RelativeLayout relativeLayout;
    int parentWidth;
    int parentHeight;

    private Button button;
    boolean check = false;
    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;

    //그리드 리싸이클러뷰
    public RecyclerView simul_recyclerview;
    private final List<Tab4_Simulation_Item> simulation_items = new ArrayList<>();
    private final Tab4_Simulation_Adatper simulationAdatper = new Tab4_Simulation_Adatper(getContext(), simulation_items, R.layout.fragment_tab4_simulation);

    FirebaseAuth firebaseAuth;
    FirebaseUser mFirebaseUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("구매자");
    private DatabaseReference myRef2 = database.getReference("부자재");

    private String cart;
    private ImageView trashView;
    private int trash_width;
    private int trash_height;


    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int parentWidth = ((ViewGroup) v.getParent()).getWidth();    // 부모 View 의 Width
            int parentHeight = ((ViewGroup) v.getParent()).getHeight();    // 부모 View 의 Height

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 뷰 누름
                oldXvalue = event.getX();
                oldYvalue = event.getY();
                Log.d("viewTest", "oldXvalue : " + oldXvalue + " oldYvalue : " + oldYvalue);    // View 내부에서 터치한 지점의 상대 좌표값.
                Log.d("viewTest", "v.getX() : " + v.getX());    // View 의 좌측 상단이 되는 지점의 절대 좌표값.
                Log.d("viewTest", "RawX : " + event.getRawX() + " RawY : " + event.getRawY());    // View 를 터치한 지점의 절대 좌표값.
                Log.d("viewTest", "v.getHeight : " + v.getHeight() + " v.getWidth : " + v.getWidth());    // View 의 Width, Height

            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // 뷰 이동 중
                v.setX(v.getX() + (event.getX()) - (v.getWidth() / 2));
                v.setY(v.getY() + (event.getY()) - (v.getHeight() / 2));

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 뷰에서 손을 뗌
                Log.d("현재위치",v.getX() +"x " + v.getY());
                Log.d("타겟",trash_width +"x " + trash_height);
                int a =(int) v.getX();
                int b = (int) v.getY();

                if( Math.abs((int) a - trash_width) <= 100 &&  Math.abs((int) b + 200 - trash_height) <= 150)
                    v.setVisibility(View.GONE);
                if (v.getX() < 0) {
                    v.setX(0);
                } else if ((v.getX() + v.getWidth()) > parentWidth) {
                    v.setX(parentWidth - v.getWidth());
                }

                if (v.getY() < 0) {
                    v.setY(0);
                } else if ((v.getY() + v.getHeight()) > parentHeight) {
                    v.setY(parentHeight - v.getHeight());
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_tab4_simulation, container, false);

        relativeLayout = (RelativeLayout) rootview.findViewById(R.id.relative);
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = firebaseAuth.getCurrentUser();
        trashView = (ImageView) rootview.findViewById(R.id.trash);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("우람", "dd");
                    if ("rnjsdnfka7@gmail.com".equals(ds.child("email").getValue().toString())) {
                        cart = ds.child("cart").getValue().toString();
                        Log.d("dd", cart);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ImageButton menubtn = rootview.findViewById(R.id.simulation_menu_button);
        ImageButton x_btn = rootview.findViewById(R.id.x_button);
        simul_menu_layout = rootview.findViewById(R.id.simul_menu_layout);
        view = rootview.findViewById(R.id.temp);
        blur = rootview.findViewById(R.id.blur);

        button = (Button) rootview.findViewById(R.id.add_btn);

        //그리드 레이아웃으로 한줄에 2개씩 제품 보여주기
        simul_recyclerview = rootview.findViewById(R.id.simulation_menu_recycler);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        simul_recyclerview.setHasFixedSize(true);
        simul_recyclerview.setLayoutManager(layoutManager);
        simul_recyclerview.setAdapter(simulationAdatper);

        simulation_items.clear();

        ViewTreeObserver viewTreeObserver = relativeLayout.getViewTreeObserver();
        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressLint("ClickableViewAccessibility")
            public void onGlobalLayout() {
                if(check) {
                    parentWidth = relativeLayout.getWidth();    // 부모 View 의 Width
                    parentHeight = relativeLayout.getHeight();    // 부모 View 의 Height
                    trash_width = (int) trashView.getX();
                    trash_height =(int) trashView.getY();

                    check = false;
                    Log.d("ㅇㅇ","가까운");
                }
            }
        };

        viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);

         check = true;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("넌 되니", "ㅇㅇ");

                ImageView iv = new ImageView(getContext());  // 새로 추가할 imageView 생성

                iv.setImageResource(R.drawable.wooram);  // imageView에 내용 추가
                Log.d("짜이", parentHeight + "");
                Log.d("우람", parentWidth + "");

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( (int)3.3 * parentWidth / 9,(int) 3.3 * parentHeight / 16);

                iv.setLayoutParams(layoutParams);  // imageView layout 설정
                iv.setOnTouchListener(touchListener);

                relativeLayout.addView(iv); // 기존 linearLayout에 imageView 추가

                }
        });
        // imageView = (ImageView) rootview.findViewById(R.id.test);

        //  imageView.setOnTouchListener(this);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                Log.d("촉",cart);
                int width = dm.widthPixels;
                int f_width = width - (int) (width * 0.8);
                simulation_items.clear();
                animation = new TranslateAnimation(width, f_width, 0, 0);
                animation.setDuration(100);
                simul_menu_layout.setVisibility(View.VISIBLE);
                simul_menu_layout.setAnimation(animation);
                animation = new TranslateAnimation(f_width, 0, 0, 0);
                animation.setDuration(25);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.VISIBLE);
                        view.setAnimation(animation);
                    }
                }, 75);

                blur.setVisibility(View.GONE);

                StringTokenizer st = new StringTokenizer(cart, "#");

                final String[] arr = new String[st.countTokens()];
                for(int i = 0; i < arr.length; i++){
                    arr[i] = st.nextToken();
                }
                myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("제발잠좀","자게해줘"+dataSnapshot.getValue().toString());
                        //String url = dataSnapshot.child("image_RB_url").child(d.getKey()).getValue().toString();
                        int i = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if ( i == arr.length)
                                break;
                            if(arr[i].equals(ds.getKey())){

                                Log.d("궁금 " + ds.getKey(),ds.child("image_data").getChildrenCount()+"");
                                String data = ds.child("image_RB_data").child(Integer.parseInt(ds.getKey()) + (int) ds.child("image_data").getChildrenCount()+"").getValue().toString();
                                int width = Integer.parseInt(ds.child("size_width").getValue().toString());
                                int height = Integer.parseInt(ds.child("size_height").getValue().toString());
                                addItemToRecyclerView(data, width, height);
                                i++;
                                continue;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        x_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

                int width = dm.widthPixels;

                animation = new TranslateAnimation(0, width, 0, 0);
                animation.setDuration(100);
                view.setVisibility(View.GONE);
                simul_menu_layout.setVisibility(View.GONE);
                simul_menu_layout.setAnimation(animation);
                blur.setVisibility(View.VISIBLE);
            }
        });


        //아이템 클릭시 상품 상세 페이지로 이동Math
        simulationAdatper.setOnItemClickListener(new Tab4_Simulation_Adatper.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                inflate(position);
            }
        });

        return rootview;

    }
    //부자재 정보 번들에 담아서 상품 상세 페이지로 이동
    @SuppressLint("ClickableViewAccessibility")
    public void inflate(int position) {
        //상품 상세 페이지 정보 가져오기
        double width = simulation_items.get(position).getWidth() / 10;
        double height = simulation_items.get(position).getHeight() / 10;
        String data = simulation_items.get(position).getData();
        ImageView iv = new ImageView(getContext());  // 새로 추가할 imageView 생성

        double randomValue = Math.random();
        int intValue = (int) (randomValue * 5) + 2;
        iv.setX( intValue * parentWidth / 9 );
        randomValue = Math.random();
        intValue = (int) (randomValue * 8) + 2;
        iv.setY( intValue * parentHeight / 16 );


        byte[] decodedByteArray = Base64.decode(data, Base64.NO_WRAP);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        iv.setImageBitmap(decodedBitmap);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache();
        //iv.setImageResource(R.drawable.wooram);  // imageView에 내용 추가
        Log.d("짜이", parentHeight + "");
        Log.d("우람", parentWidth + "");


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( (int) width* parentWidth / 9,(int) height* parentHeight / 16);

        iv.setLayoutParams(layoutParams);  // imageView layout 설정
        iv.setOnTouchListener(touchListener);

        relativeLayout.addView(iv); // 기존 linearLayout에 imageView 추가

/*        String name = category_item.get(position).getName();
        String price = category_item.get(position).getPrice();
        String[] url = category_item.get(position).getImg_url();
        String width = category_item.get(position).getWidth();
        String height = category_item.get(position).getHeight();
        String depth = category_item.get(position).getDepth();
        String keyword = category_item.get(position).getKeyword();
        String stock = category_item.get(position).getStock();
        String storename = category_item.get(position).getStorename();
        String unique_num = category_item.get(position).getUnique_number();

        Fragment tab1 = new Product_Detail_Fragment();

        //번들에 부자재 상세정보 담아서 가게 상세 페이지 프래그먼트로 보내기
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("price", price);
        bundle.putStringArray("url", url);
        bundle.putString("width", width);
        bundle.putString("height", height);
        bundle.putString("depth", depth);
        bundle.putString("keyword", keyword);
        bundle.putString("stock", stock);
        bundle.putString("storename", storename);
        bundle.putString("unique_number", unique_num);
        tab1.setArguments(bundle);

        //프래그먼트 카테고리 검색 -> 제품 상세 페이지로 교체
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                .replace(R.id.main_tab_view, tab1)
                .hide(HomeSearch_Category.this)
                .addToBackStack(null)
                .commit();

 */
    }
    public void addItemToRecyclerView(String data, int width, int height){
        Tab4_Simulation_Item item = new Tab4_Simulation_Item(data, width, height);
        simulation_items.add(item);

        simulationAdatper.notifyDataSetChanged();
    }


}
