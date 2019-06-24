package com.cloudminds.feedback.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IFUserFeedback {

    @POST
    Call<ResponseBody> uploadImages(@Url String url, @Body ImageBody requestBody);

    class ImageBody {
        public int _id;
        public int report_id;
        public int task_flag;
        public String content;
        public String module;
        public String app_version;
        public String error_type;
        public String error_time;
        public String device_model;
        public String imei1;
        public String build_version;
        public String hardware_version;
        public String android_version;
        public String storage_usage;
        public String memory_usage;
        public String cpu_usage;
        public String network_type;
        public String battery_level;
        public String attachment_name;
        public String attachment_size;
        public int is_monkey;

        public ImageBody() {
        }

        public ImageBody(int _id, int report_id, int task_flag, String content, String module,
                         String app_version, String error_type, String error_time, String device_model,
                         String imei1, String build_version, String hardware_version, String android_version,
                         String storage_usage, String memory_usage, String cpu_usage, String network_type,
                         String battery_level, String attachment_name, String attachment_size, int is_monkey) {
            this._id = _id;
            this.report_id = report_id;
            this.task_flag = task_flag;
            this.content = content;
            this.module = module;
            this.app_version = app_version;
            this.error_type = error_type;
            this.error_time = error_time;
            this.device_model = device_model;
            this.imei1 = imei1;
            this.build_version = build_version;
            this.hardware_version = hardware_version;
            this.android_version = android_version;
            this.storage_usage = storage_usage;
            this.memory_usage = memory_usage;
            this.cpu_usage = cpu_usage;
            this.network_type = network_type;
            this.battery_level = battery_level;
            this.attachment_name = attachment_name;
            this.attachment_size = attachment_size;
            this.is_monkey = is_monkey;
        }
    }
}
