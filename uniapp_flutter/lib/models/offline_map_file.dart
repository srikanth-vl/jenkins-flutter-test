class OfflineMapFile {
  String fileName;
  String fileUrl;
  String fileStoragePath;
  String fileSize;
  bool isDownloaded = false;
  bool isDownloading = false;
  Map<String, String> fileAdditionalInfo;

  OfflineMapFile(
      {this.fileName,
      this.fileUrl,
      this.fileStoragePath,
      this.fileSize,
      this.fileAdditionalInfo});

  OfflineMapFile.fromJson(Map<String, dynamic> json) {
    fileName = json['file_name'];
    fileUrl = json['file_url'];
    fileStoragePath = json['file_storage_path'];
    fileSize = json['file_size'];
    if (json['file_additional_info'] != null) {
      fileAdditionalInfo = new Map();
      if (json['offline_map_files'] != null) {
        json['offline_map_files'].forEach((key, v) {
          fileAdditionalInfo[key] = v.toString();
        });
      }
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['file_name'] = this.fileName;
    data['file_url'] = this.fileUrl;
    data['file_storage_path'] = this.fileStoragePath;
    data['file_size'] = this.fileSize;
    data['file_additional_info'] = this.fileAdditionalInfo;
    return data;
  }
}
