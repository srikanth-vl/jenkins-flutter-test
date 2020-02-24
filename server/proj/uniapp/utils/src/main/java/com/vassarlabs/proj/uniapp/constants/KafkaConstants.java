package com.vassarlabs.proj.uniapp.constants;

public class KafkaConstants {
	public static final String FORM_SUBMIT_RECEIVER_CLASS = "form.submit.Receiver.Class";
	public static final String MEDIA_SUBMIT_RECEIVER_CLASS = "media.submit.Receiver.Class";
	public static final String KAFKA_CONSUMER_COUNT = "no_of_kafka_consumers";
	public static final String KAFKA_BROKERS = "${kafka_brokers}";
	public static final String KAFKA_GROUP_ID_CONFIG = "${group_id_config1}";
	public static final String MAX_POLL_RECORDS = "${max_poll_records}";
	public static final String OFFSET_RESET_EARLIER = "${offset_reset_earliest}";
	public static final String DATA_TOPIC = "${data_topic}";
	public static final String MEDIA_TOPIC = "${media_topic}";
	public static final String FAILED_DATA_TOPIC = "${failed_data_topic}";
	public static final String FAILED_MEDIA_TOPIC = "${failed_media_topic}";
	public static final String FAILED_SUBMISSION_FORM_DATA_TOPIC = "${failed_submission_form_data_topic}";
	public static final String MAP_BBOX_DATA_TOPIC = "${map_bbox_data_topic}";
	public static final String USER_META_DATA_TOPIC = "${user_meta_data_topic}";
	public static final long FAILED_DATA_MAX_POLL_TIME_IN_MILLIS = 6000l;
}
