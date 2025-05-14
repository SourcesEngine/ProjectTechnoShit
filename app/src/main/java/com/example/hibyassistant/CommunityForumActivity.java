package com.example.hibyassistant;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hibyassistant.adapters.MessageAdapter;
import com.example.hibyassistant.adapters.PostAdapter;
import com.example.hibyassistant.adapters.NotificationsAdapter;
import com.example.hibyassistant.models.Comment;
import com.example.hibyassistant.models.Message;
import com.example.hibyassistant.models.Notification;
import com.example.hibyassistant.models.Post;
import com.example.hibyassistant.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityForumActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private EditText messageInput;
    private List<User> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_forum);

        try {
            // Check if user is signed in
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return;
            }

            // Initialize Firebase instances
            db = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();

            // Initialize permission launcher
            requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean isGranted : permissions.values()) {
                        if (!isGranted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        launchImagePicker();
                    } else {
                        Toast.makeText(this, "Storage permission is required to upload images", Toast.LENGTH_SHORT).show();
                    }
                }
            );

            // Initialize posts list
            posts = new ArrayList<>();
            searchResults = new ArrayList<>();

            // Initialize views
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            RecyclerView messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
            messageInput = findViewById(R.id.messageInput);
            ImageButton sendButton = findViewById(R.id.sendButton);

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Community Forum");
            }

            // Initialize PostAdapter with all required parameters
            postAdapter = new PostAdapter(posts,
                this::onLikeClick,
                this::onCommentClick,
                this::onProfileClick,
                this::onPostLongPress);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(postAdapter);

            // Initialize RecyclerView for messages
            List<Message> messages = new ArrayList<>();
            MessageAdapter adapter = new MessageAdapter(messages);
            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            messagesRecyclerView.setAdapter(adapter);

            // Set up image picker launcher
            imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                showCreatePostDialog();
                            }
                        } catch (Exception e) {
                            Log.e("CommunityForum", "Error handling image selection", e);
                            Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
                            selectedImageUri = null;
                        }
                    }
                }
            );

            // Set up bottom navigation
            BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
            bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    loadPosts();
                    return true;
                } else if (itemId == R.id.navigation_search) {
                    showSearchDialog();
                    return true;
                } else if (itemId == R.id.navigation_add) {
                    showImagePickerOrCreatePost();
                    return true;
                } else if (itemId == R.id.navigation_notifications) {
                    showNotifications();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    showProfile();
                    return true;
                }
                return false;
            });

            // Set up send button click listener
            sendButton.setOnClickListener(v -> sendMessage());

            // Load posts
            loadPosts();
        } catch (Exception e) {
            Log.e("CommunityForum", "Error initializing activity", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset image selection state when returning to the activity
        selectedImageUri = null;
        // Reload posts to ensure fresh data
        loadPosts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear any pending operations
        selectedImageUri = null;
    }

    private void loadPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                posts.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            String postId = doc.getId();
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setId(postId);
                                
                                // Get media URLs with proper type casting
                                List<String> mediaUrls = doc.get("mediaUrls", List.class);
                                if (mediaUrls != null) {
                                    post.setMediaUrls(mediaUrls);
                                }
                                
                                // Ensure required fields are not null
                                if (post.getUserId() == null) {
                                    post.setUserId("");
                                }
                                if (post.getUserName() == null) {
                                    post.setUserName("Unknown User");
                                }
                                if (post.getContent() == null) {
                                    post.setContent("");
                                }
                                
                                posts.add(post);
                            }
                        } catch (Exception e) {
                            Log.e("CommunityForum", "Error processing post: " + e.getMessage());
                            continue;
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                }
            });
    }

    private void showCreatePostDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
            EditText contentEditText = view.findViewById(R.id.contentEditText);
            ImageView postImageView = view.findViewById(R.id.postImageView);

            if (selectedImageUri != null) {
                try {
                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(postImageView);
                    postImageView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e("CommunityForum", "Error loading image preview", e);
                    Toast.makeText(this, "Error loading image preview", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null;
                    return;
                }
            }

            AlertDialog dialog = builder.setView(view)
                .setTitle("Create Post")
                .setPositiveButton("Post", (dialogInterface, which) -> {
                    String content = contentEditText.getText().toString().trim();
                    if (!content.isEmpty() || selectedImageUri != null) {
                        createPost(content);
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    selectedImageUri = null;
                })
                .create();

            dialog.setOnDismissListener(dialogInterface -> {
                selectedImageUri = null;
            });

            dialog.show();
        } catch (Exception e) {
            Log.e("CommunityForum", "Error showing create post dialog", e);
            Toast.makeText(this, "Error showing post dialog", Toast.LENGTH_SHORT).show();
            selectedImageUri = null;
        }
    }

    private void createPost(String content) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to create a post", Toast.LENGTH_SHORT).show();
            selectedImageUri = null;
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        
        // First upload image if exists
        if (selectedImageUri != null) {
            // Show loading progress
            Toast.makeText(this, "Preparing to upload image...", Toast.LENGTH_SHORT).show();

            try {
                // Create a unique filename using timestamp
                String timestamp = String.valueOf(System.currentTimeMillis());
                String imageFileName = "post_" + timestamp + ".jpg";
                
                // Create storage reference with explicit path
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference postImagesRef = storageRef
                    .child("post_images")
                    .child(userId)
                    .child(imageFileName);

                // Create file metadata
                StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

                // Upload the image with metadata
                UploadTask uploadTask = postImagesRef.putFile(selectedImageUri, metadata);

                uploadTask
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Toast.makeText(this, "Upload progress: " + (int)progress + "%", Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL after successful upload
                        postImagesRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Create array of media URLs
                                List<String> mediaUrls = new ArrayList<>();
                                mediaUrls.add(uri.toString());
                                
                                // Create post with image URL
                                createPostInFirestore(content, mediaUrls);
                            })
                            .addOnFailureListener(e -> {
                                String errorMessage = "Error getting download URL: " + e.getMessage();
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                                Log.e("CommunityForum", errorMessage, e);
                                selectedImageUri = null;
                            });
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Error uploading image: " + e.getMessage();
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("CommunityForum", errorMessage, e);
                        selectedImageUri = null;
                    });
            } catch (Exception e) {
                String errorMessage = "Error preparing upload: " + e.getMessage();
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("CommunityForum", errorMessage, e);
                selectedImageUri = null;
            }
        } else {
            // Create post without image
            createPostInFirestore(content, new ArrayList<>());
        }
    }

    private void createPostInFirestore(String content, List<String> mediaUrls) {
        String userId = mAuth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String userName = documentSnapshot.getString("name");
                String userProfileImage = documentSnapshot.getString("profileImage");

                // Create post data
                Map<String, Object> postData = new HashMap<>();
                postData.put("userId", userId);
                postData.put("userName", userName);
                postData.put("userProfileImage", userProfileImage);
                postData.put("content", content);
                postData.put("mediaUrls", mediaUrls);
                postData.put("timestamp", timestamp);
                postData.put("likes", 0);
                postData.put("comments", 0);
                
                // Add to Firestore (this will auto-generate the ID)
                db.collection("posts")
                    .add(postData)
                    .addOnSuccessListener(documentReference -> {
                        String postId = documentReference.getId();  // Get the auto-generated ID
                        Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
                        selectedImageUri = null;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // If post creation fails and we uploaded an image, we should delete it
                        if (!mediaUrls.isEmpty()) {
                            try {
                                storage.getReferenceFromUrl(mediaUrls.get(0)).delete();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error getting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // If getting user data fails and we uploaded an image, we should delete it
                if (!mediaUrls.isEmpty()) {
                    try {
                        storage.getReferenceFromUrl(mediaUrls.get(0)).delete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
    }

    private void onLikeClick(Post post) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference postRef = db.collection("posts").document(post.getId());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(postRef);
            if (!snapshot.exists()) {
                try {
                    throw new Exception("Post not found");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            Post currentPost = snapshot.toObject(Post.class);
            if (currentPost == null) {
                try {
                    throw new Exception("Error reading post data");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Toggle like
            boolean wasLiked = currentPost.isLikedByUser(userId);
            currentPost.toggleLike(userId);
            transaction.set(postRef, currentPost);

            // Create notification for post owner if it's a new like
            if (!wasLiked && !currentPost.getUserId().equals(userId)) {
                createNotification(currentPost.getUserId(), "New Like", 
                    mAuth.getCurrentUser().getDisplayName() + " liked your post", post.getId());
            }

            return null;
        })
        .addOnSuccessListener(aVoid -> {
            // Update UI
            postAdapter.notifyDataSetChanged();
        })
        .addOnFailureListener(e -> {
            Log.e("CommunityForum", "Error updating like: " + e.getMessage());
            Toast.makeText(this, "Error updating like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void onCommentClick(Post post) {
        // Show comments dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_comments, null);
        RecyclerView commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        EditText commentInput = view.findViewById(R.id.commentInput);

        // Set up comments adapter and load comments
        // ... (implement CommentsAdapter and set it up here)

        builder.setView(view)
               .setTitle("Comments")
               .setPositiveButton("Add Comment", (dialog, which) -> {
                   String commentText = commentInput.getText().toString().trim();
                   if (!commentText.isEmpty()) {
                       addComment(post, commentText);
                   }
               })
               .setNegativeButton("Close", null)
               .show();
    }

    private void addComment(Post post, String content) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to add comments", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();
        String userProfileImage = mAuth.getCurrentUser().getPhotoUrl() != null ? 
            mAuth.getCurrentUser().getPhotoUrl().toString() : "";

        // Create comment object
        Comment comment = new Comment(userId, userName, userProfileImage, content);
        
        // Add comment to post's subcollection
        db.collection("posts").document(post.getId())
            .collection("comments")
            .add(comment)
            .addOnSuccessListener(documentReference -> {
                // Update comment count
                db.collection("posts").document(post.getId())
                    .update("comments", post.getComments() + 1)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                        
                        // Create notification for post owner
                        if (!post.getUserId().equals(userId)) {
                            createNotification(post.getUserId(), "New Comment", 
                                userName + " commented on your post", post.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CommunityForum", "Error updating comment count: " + e.getMessage());
                        Toast.makeText(this, "Error updating comment count", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("CommunityForum", "Error adding comment: " + e.getMessage());
                Toast.makeText(this, "Error adding comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void onPostLongPress(Post post, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.post_options_menu);
        
        // Only show edit/delete options for the post owner
        if (!post.getUserId().equals(mAuth.getCurrentUser().getUid())) {
            popup.getMenu().removeItem(R.id.menu_edit_post);
            popup.getMenu().removeItem(R.id.menu_delete_post);
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit_post) {
                showEditPostDialog(post);
                return true;
            } else if (itemId == R.id.menu_delete_post) {
                showDeletePostConfirmation(post);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void showEditPostDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
        EditText contentEditText = view.findViewById(R.id.contentEditText);
        ImageView postImageView = view.findViewById(R.id.postImageView);

        contentEditText.setText(post.getContent());
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(post.getImageUrl())
                .into(postImageView);
            postImageView.setVisibility(View.VISIBLE);
        }

        builder.setView(view)
            .setTitle("Edit Post")
            .setPositiveButton("Save", (dialog, which) -> {
                String content = contentEditText.getText().toString().trim();
                if (!content.isEmpty()) {
                    updatePost(post, content);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updatePost(Post post, String newContent) {
        db.collection("posts").document(post.getId())
            .update("content", newContent)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error updating post", Toast.LENGTH_SHORT).show());
    }

    private void showDeletePostConfirmation(Post post) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete", (dialog, which) -> deletePost(post))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deletePost(Post post) {
        // Delete post image if exists
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            storage.getReferenceFromUrl(post.getImageUrl()).delete();
        }

        // Delete post document
        db.collection("posts").document(post.getId())
            .delete()
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error deleting post", Toast.LENGTH_SHORT).show());
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        SearchView searchView = view.findViewById(R.id.searchView);
        RecyclerView searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 3) {
                    searchUsers(newText);
                }
                return true;
            }
        });

        builder.setView(view)
               .setTitle("Search Users")
               .setNegativeButton("Close", null)
               .show();
    }

    private void searchUsers(String query) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .limit(20)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                searchResults.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    User user = new User(
                        doc.getId(),
                        doc.getString("name"),
                        doc.getString("email"),
                        doc.getString("photoUrl")
                    );
                    searchResults.add(user);
                }
                // Update UI with search results
                updateSearchResults();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error searching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateSearchResults() {
        // Create and show a dialog with search results
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        if (searchResults.isEmpty()) {
            builder.setMessage("No users found");
        } else {
            String[] userNames = new String[searchResults.size()];
            for (int i = 0; i < searchResults.size(); i++) {
                userNames[i] = searchResults.get(i).getName();
            }

            builder.setItems(userNames, (dialog, which) -> {
                User selectedUser = searchResults.get(which);
                startChat(selectedUser);
            });
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void startChat(User user) {
        // Navigate to chat with selected user
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userId", user.getId());
        intent.putExtra("userName", user.getName());
        startActivity(intent);
    }

    private void createNotification(String userId, String title, String message, String postId) {
        if (userId == null || userId.equals(mAuth.getCurrentUser().getUid())) {
            return; // Don't create notification for self or invalid user
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("postId", postId);
        notificationData.put("senderId", currentUserId);
        notificationData.put("senderName", mAuth.getCurrentUser().getDisplayName());
        notificationData.put("read", false);

        db.collection("users").document(userId)
            .collection("notifications")
            .add(notificationData)
            .addOnSuccessListener(documentReference -> {
                // Send FCM notification
                sendFCMNotification(userId, title, message);
            })
            .addOnFailureListener(e -> {
                Log.e("CommunityForum", "Error creating notification: " + e.getMessage());
            });
    }

    private void sendFCMNotification(String userId, String title, String message) {
        // Get the FCM token for the user
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String fcmToken = documentSnapshot.getString("fcmToken");
                if (fcmToken != null && !fcmToken.isEmpty()) {
                    // Send FCM message
                    Map<String, String> data = new HashMap<>();
                    data.put("title", title);
                    data.put("message", message);
                    data.put("type", "notification");

                    // Create FCM message
                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .send(new com.google.firebase.messaging.RemoteMessage.Builder(fcmToken)
                            .setData(data)
                            .build());
                }
            })
            .addOnFailureListener(e -> {
                Log.e("CommunityForum", "Error sending FCM notification: " + e.getMessage());
            });
    }

    private void showNotifications() {
        String userId = mAuth.getCurrentUser().getUid();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_notifications, null);
        RecyclerView notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        
        // Set up notifications adapter
        List<Notification> notifications = new ArrayList<>();
        NotificationsAdapter notificationsAdapter = new NotificationsAdapter(notifications);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationsAdapter);
        
        db.collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                notifications.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Notification notification = doc.toObject(Notification.class);
                    notification.setId(doc.getId());
                    notifications.add(notification);
                }
                notificationsAdapter.updateNotifications(notifications);
                builder.setView(view)
                       .setTitle("Notifications")
                       .setPositiveButton("Close", null)
                       .show();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show());
    }

    private void onProfileClick(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user profile", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void showImagePickerOrCreatePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Post")
               .setItems(new String[]{"Upload Photo", "Write Text"}, (dialog, which) -> {
                   if (which == 0) {
                       // Check and request permissions before launching image picker
                       checkAndRequestPermissions();
                   } else {
                       // Write text only
                       showCreatePostDialog();
                   }
               })
               .show();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            requestPermissionLauncher.launch(new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            });
        } else {
            // Android 12 and below
            requestPermissionLauncher.launch(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        // Navigate to profile activity
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to send messages", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();

        Map<String, Object> message = new HashMap<>();
        message.put("userId", userId);
        message.put("userName", userName);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());

        db.collection("user_messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                messageInput.setText("");
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}