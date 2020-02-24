angular
.module('detailModule')
.constant('image_table_md',{
 "imageDataTable": {

   "headers": [{
     "name": "Date",
   },
   {
     "name": "Timestamp",

   },
   {
     "name":"formdata"
   },
   {
     "name":"Images"
   }
 ],
 "dataKeys": [{
   "jsonkey": "timestamp",
   "colorClass": "thc1",
   "dataType": "TIMESTAMP"
 },
 {
   "jsonkey": "timestamp",
   "colorClass": "thc1",
   "dataType": "FINALLOCATION"
 },
 {
   //"loop": false,
   "jsonkey": "formdata",
   "colorClass": "thc1",
   "dataType": "FORMDATA"
 },
 {
   "jsonkey": "image",
   "colorClass": "thc1",
   "dataType": "IMAGEARRAY"
 },

]
}
})
.constant('awsbuckets',{
  "awsbuckets":[
    {
        "superappid" : "d0bb80aa-bb86-39b6-a351-13f02e72752b",
        "awsurl" : "https://s3-us-west-2.amazonaws.com/dmedu/",
    },
    {
        "superappid": "03494406-0e7b-11e9-a47f-0947e4631ca5",
         "awsurl" :"https://s3-us-west-2.amazonaws.com/apiiatp/",
    }

  ]
})
