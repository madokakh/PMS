package com.example.scheduleproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.api.services.calendar.model.EventAttachment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AttachmentsAdapter extends RecyclerView.Adapter<AttachmentsAdapter.ViewHolder> {

    private List<EventAttachment> attachments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EventAttachment attachment);
    }

    public AttachmentsAdapter(List<EventAttachment> attachments, OnItemClickListener listener) {
        this.attachments = attachments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventAttachment attachment = attachments.get(position);
        Log.d("AttachmentsAdapter", "Binding attachment: " + attachment.getFileUrl() + ", MIME type: " + attachment.getMimeType());
        holder.bind(attachment, listener);
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.attachment_image);
            tvTitle = itemView.findViewById(R.id.text_title);
        }

        public void bind(EventAttachment attachment, OnItemClickListener listener) {
            String url = attachment.getFileUrl();
            String title = attachment.getTitle();
            Log.d("Title",title);
            Log.d("TitleW",getImageNameWithoutExtension(title));
            tvTitle.setText(getImageNameWithoutExtension(title));
            if (attachment.getMimeType().startsWith("image/")) {
                if (url.contains("drive.google.com")) {
                    url = convertGoogleDriveUrlToDirect(url);
                   // url = "https://drive.google.com/file/d/1NofWohP62aKg_bMkT9dNxnXTDoTzzTyT/view";
                }
                Picasso.get()
                        .load(url)
                        .error(R.drawable.baseline_error_24)  // Add an error placeholder
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("Picasso", "Image loaded successfully: " );
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("Picasso", "Error loading image: " , e);
                            }
                        });
            } else if (attachment.getMimeType().equals("application/pdf")) {
                imageView.setImageResource(R.drawable.ic_pdf); // Use a PDF icon
                url = convertGoogleDriveUrlToViewer(url);
            }
            itemView.setOnClickListener(v -> listener.onItemClick(attachment));
        }

        private String convertGoogleDriveUrlToDirect(String url) {
            if (url.contains("open?id=")) {
                return url.replace("open?id=", "uc?export=download&id=");
            } else if (url.contains("/file/d/")) {
                String fileId = url.split("/file/d/")[1].split("/")[0];
                return "https://drive.google.com/uc?export=download&id=" + fileId;
            }
            return url;
        }
        private String convertGoogleDriveUrlToViewer(String url) {
            if (url.contains("open?id=")) {
                return url.replace("open?id=", "file/d/") + "/view";
            } else if (url.contains("/file/d/")) {
                String fileId = url.split("/file/d/")[1].split("/")[0];
                return "https://drive.google.com/file/d/" + fileId + "/view";
            }
            return url;
        }

        public String getImageNameWithoutExtension(String attachmentName) {
            if (attachmentName == null || attachmentName.isEmpty()) {
                return "";
            }

            int dotIndex = attachmentName.lastIndexOf('.');
            if (dotIndex == -1) {
                return attachmentName; // No extension found
            }

            return attachmentName.substring(0, dotIndex);
        }
    }
}
