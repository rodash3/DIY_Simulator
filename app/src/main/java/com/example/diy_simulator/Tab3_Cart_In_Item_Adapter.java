package com.example.diy_simulator;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Tab3_Cart_In_Item_Adapter extends RecyclerView.Adapter<Tab3_Cart_In_Item_Adapter.ViewHolder>  {
    Context context;
    List<Tab3_Cart_In_Item_Info> items;
    int item_layout;

    FirebaseDatabase database= FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("구매자");

    public Tab3_Cart_In_Item_Adapter(Context context, List<Tab3_Cart_In_Item_Info> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    public interface OnItemImageClickListener {
        void onItemImageClick(View v, int pos);
    }

    // 리스너 객체 참조를 저장하는 변수
    private Tab3_Cart_In_Item_Adapter.OnItemImageClickListener iListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemImageClickListener(Tab3_Cart_In_Item_Adapter.OnItemImageClickListener listener) {
        this.iListener = listener ;
    }


    public interface OnCheckItemClickListener {
        void onCheckItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private Tab3_Cart_In_Item_Adapter.OnCheckItemClickListener cListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnCheckItemClickListener(Tab3_Cart_In_Item_Adapter.OnCheckItemClickListener listener) {
        this.cListener = listener ;
    }


    public interface OnXItemClickListener {
        void onXItemClick(View v);
    }

    // 리스너 객체 참조를 저장하는 변수
    private Tab3_Cart_In_Item_Adapter.OnXItemClickListener xListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnXItemClickListener(Tab3_Cart_In_Item_Adapter.OnXItemClickListener listener) {
        this.xListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    // 리스너 객체 참조를 저장하는 변수
    private Tab3_Cart_In_Item_Adapter.OnItemClickListener mListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(Tab3_Cart_In_Item_Adapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab3_cart_item_in_item, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Tab3_Cart_In_Item_Info item = items.get(position);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();

        //제품 이름, 가격, 가게 이름 텍스트 나타내기
        holder.name.setText(item.getName());
        holder.price.setText(item.getPrice() + " 원");
        holder.store_name.setText(item.getStorename());
        holder.amount.setText(String.valueOf(item.getAmount()));

        //in some cases, it will prevent unwanted situations
        holder.check.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.check.setChecked(item.getCheckBox());

        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                item.setCheckBox(isChecked);
            }
        });

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.placeholder(R.drawable.mungmung);

        //제품 이미지 url로 나타내기
        if (!TextUtils.isEmpty(item.getPreview_img_url())) {
            Glide.with(holder.itemView.getContext())
                    .setDefaultRequestOptions(requestOptions)
                    .load(item.getPreview_img_url())
                    .into(holder.img);
        }

        // X 버튼 누르면 해당 아이템 삭제
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // 다이얼로그 띄워서 삭제 의사 묻기
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("선택한 상품을 삭제하시겠습니까?");
                // '네' 클릭시 아이템 삭제
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 아이템 리스트에서 제거
                        items.remove(position);
                        notifyDataSetChanged();
                        xListener.onXItemClick(v);
                        //if(items.size() == 0) tab3.isEmptyCart();

                        // 구매자 DB - cart 에서 해당 번호 삭제
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    assert mFirebaseUser != null;
                                    if(mFirebaseUser.getEmail().equals(ds.child("email").getValue().toString())){
                                        String cart = ds.child("cart").getValue().toString();
                                        //해당 부자재 번호 삭제
                                        cart = cart.replaceFirst(item.getUnique_number(), "");
                                        //가운데에서 삭제되었을 경우 삭제되고 남은 ## 를 #으로 변경
                                        cart = cart.replace("##","#");
                                        //맨 앞에서 삭제되었을 경우 맨 앞에 남은 # 지우기
                                        if(cart.startsWith("#")) cart = cart.substring(1);
                                        //맨 뒤에서 삭제되었을 경우 맨 뒤에 남은 # 지우기
                                        if(cart.endsWith("#")) cart = cart.substring(0, cart.length()-1);
                                        myRef.child(ds.getKey()).child("cart").setValue(cart);
                                        break;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

                // '아니오' 클릭시 동작 없음
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // 이미지 클릭시 상품 정보 상세 페이지로 이동
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iListener.onItemImageClick(v, position);
            }
        });

        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cListener.onCheckItemClick(v, position);
            }
        });

        // + 버튼 누르면 수량 1 증가
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stock = Integer.parseInt(item.getStock());
                int amount = item.getAmount();
                // 현재 담은 수량이 재고보다 작을 경우 1 증가
                if(stock > amount){
                    holder.amount.setText(String.valueOf(item.getAmount()+1));
                    item.setAmount(item.getAmount()+1);
                    mListener.onItemClick(v, position);
                    // 해당 수량에 맞게 가격 재설정
                    //tab3.setSum_of_money();
                }
                // 현재 담은 수량이 재고와 같으면 +1 불가능
                else{
                    Toast toast = Toast.makeText(v.getContext(), "장바구니에 담으려는 수량이 주문 가능 수량보다 많습니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
            }
        });

        // - 버튼 누르면 수량 1 감소
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 수량이 1보다 클 경우에만 감소
                if(item.getAmount()>1){
                    holder.amount.setText(String.valueOf(item.getAmount()-1));
                    item.setAmount(item.getAmount()-1);
                    mListener.onItemClick(v, position);
                    // 해당 수량에 맞게 가격 재설정
                    //tab3.setSum_of_money();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button del;
        ImageView img;
        TextView store_name;
        TextView name;
        TextView price;
        TextView amount;
        Button plus;
        Button minus;
        CheckBox check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            del = itemView.findViewById(R.id.cart_delete_btn);
            img = itemView.findViewById(R.id.cart_preview_img);
            store_name = itemView.findViewById(R.id.cart_product_store);
            name = itemView.findViewById(R.id.cart_product_name);
            price = itemView.findViewById(R.id.cart_product_price);
            amount = itemView.findViewById(R.id.cart_product_amount);
            plus = itemView.findViewById(R.id.cart_plus_btn);
            minus = itemView.findViewById(R.id.cart_minus_btn);
            check = itemView.findViewById(R.id.cart_check);
        }
    }

}
