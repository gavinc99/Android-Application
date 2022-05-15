package adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Project.colab.ChatActivity;
import com.Project.colab.R;
import models.Modeluser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapters);
    }

    public static class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

        Context context;
        List<Modeluser.ModelUser> userList;

        //constructor
        public AdapterUsers(Context context, List<Modeluser.ModelUser> userList) {
            this.context = context;
            this.userList = userList;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            //inflate layout(row_user.xml)
            View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            //get data
            final String hisUID = userList.get(position).getUid();
            String userImage = userList.get(position).getImage();
            String userName = userList.get(position).getName();
            String userEmail = userList.get(position).getEmail();

            //set data
            holder.mNameTv.setText(userName);
            holder.mEmailTv.setText(userEmail);
            try{
                Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);
            }
            catch(Exception e){

            }

            //handle item click
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                /*Click user from user list to start chatting.
                Start activity wuth UID of receiver
                use UID to identify the user*/
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        //view holder class
        class MyHolder extends RecyclerView.ViewHolder{

            ImageView mAvatarIv;
            TextView mNameTv, mEmailTv;


            public MyHolder(@NonNull View itemView) {
                super(itemView);

                //init views
                mAvatarIv=itemView.findViewById(R.id.avatarIv);
                mNameTv=itemView.findViewById(R.id.nameTv);
                mEmailTv=itemView.findViewById(R.id.emailTv);

            }
        }
    }
}
