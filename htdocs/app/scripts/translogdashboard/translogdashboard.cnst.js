angular
.module('translogdashboard')
.constant('translogdashboard_table_md',{

  /*DEFINE YOUR TABLETYPE IN THIS WAY*/
  /*EXAMPLE*/
  "translogdashboardTable": {
    // "sortOrder": false,
    // "totalkey": "0",
    // "totaltype": "inside",
    // "colorClass": "thc5",
    "headers": [{
      "name": "MI Tank Name",
    },
    
    {
      "name": "MI Tank Id",
    },
    {
      "name": "Data Received Date",
    },
    {
      "name": "Data Submited Date"
    },
    {
      "name": "Data",
    },


  ],
  "dataKeys": [{
    "jsonkey": "proj_name",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "school_name",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  // {
  // 	//"loop": false,
  // 	"jsonkey": "insertTs",
  // 	"colorClass": "thc1",
  // 	"dataType": 'TIMESTAMP'
  // },
  // {
  // 	//"loop": false,
  // 	"jsonkey": "dateOfTxn",
  // 	"colorClass": "thc1",
  // 	"dataType": "FINALLOCATION"
  // },
  {
    //"loop": false,
    //"mainDataKey": "",
    "jsonkey": "insert_ts",
    "colorClass": "thc1",
    "dataType": "TIMESTAMP"
    //"colorClass": "thc1"
  },
  {
    //"loop": false,
    // "mainDataKey": "classWiseOmrCountDataMap&&6",
    "jsonkey": "event_name",
    "dataType": "FINALLOCATION"
    // "colorClass": "thc2"
  },
  {
    //"loop": false,
    //"mainDataKey": "classWiseOmrCountDataMap&&7",
    "jsonkey": "input_data",
    "dataType": "DATAFORMATTER"
    // "colorClass": "thc2"
  },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&7",
  // 	"jsonkey": "omrUploadCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc2"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&8",
  // 	"jsonkey": "studentCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc2"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&8",
  // 	"jsonkey": "omrUploadCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc2"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&9",
  // 	"jsonkey": "studentCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc2"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&9",
  // 	"jsonkey": "omrUploadCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc3"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&10",
  // 	"jsonkey": "studentCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc4"
  // },
  // {
  // 	"loop": false,
  // 	"mainDataKey": "classWiseOmrCountDataMap&&10",
  // 	"jsonkey": "omrUploadCount",
  // 	"dataType": "NUMBERS"
  // 	// "colorClass": "thc4"
  // }
]
}
//
// /*FOR DEMO PURPOSE ONLY*/
//   "cfcTable" : {
//           // "sortOrder": false,
//           // "colorClass" : "thc5",
//           // "totalkey": "-1",
//           // "totaltype": "inside",
//           // "suppresscolumns" : "entityName,totalOmrSubmittedCount,classWiseOmrCountDataMap.6.studentCount",
//           "headers": [
//                     {
//                       "type": "dynamic",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "Total Students",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "Total Uploaded OMR's",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "Uploaded Percentage",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VI Class",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VI Class Uploaded",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VII Class",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VII Class Uploaded",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VIII Class",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "VIII Class Uploaded",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "IX Class",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "IX Class Uploaded",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "X Class",
//                       "rows": "1"
//                     },
//                     {
//                       "name": "X Class Uploaded",
//                       "rows": "1"
//                     }
//                   ],
//           "dataKeys": [
//                    {
//                       "jsonkey": "dynamic",
//                       "colorClass": "thc1",
//                       "dataType" : "LOCATION"
//                    },
//                    {
//                     "loop": false,
//                     "jsonkey": "totalStudentRegistered##totalOmrSubmittedCount",
//                     "colorClass": "thc1",
//                     "dataType" : "CUMMVAR"
//                   },
//                   {
//                    "loop": false,
//                    "jsonkey": "totalOmrSubmittedCount",
//                    "colorClass": "thc1",
//                    "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                     "jsonkey": "uploadedPercentage",
//                     "colorClass": "thc1",
//                     "dataType" : "PERCENTAGEWITHDASH"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&6",
//                     "jsonkey": "classWiseOmrCountDataMap.6.studentCount##classWiseOmrCountDataMap.6.omrUploadCount",
//                     "dataType" : "CUMMNUM"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&6",
//                     "jsonkey": "classWiseOmrCountDataMap.6.omrUploadCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&7",
//                     "jsonkey": "classWiseOmrCountDataMap.7.studentCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&7",
//                     "jsonkey": "classWiseOmrCountDataMap.7.omrUploadCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&8",
//                     "jsonkey": "classWiseOmrCountDataMap.8.studentCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&8",
//                     "jsonkey": "classWiseOmrCountDataMap.8.omrUploadCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                   //  "mainDataKey": "classWiseOmrCountDataMap&9",
//                     "jsonkey": "classWiseOmrCountDataMap.9.omrUploadCount",
//                     "dataType" : "NUMBERS"
//                   },
//                   {
//                     "loop": false,
//                 //    "mainDataKey": "classWiseOmrCountDataMap&9",
//                     "jsonkey": "classWiseOmrCountDataMap.9.studentCount",
//                     "dataType" : "NUMBERS"
//                   }
//                 ]
//         },
//
//   "cfcTable3" : {
//           // "sortOrder": false,
//           // "colorClass" : "thc6",
//           // "totalkey": "-1",
//           // "totaltype": "inside",
//           // "sortDistrictsBy" : "ids",
//           // "sortKey":"entityName",
//           // "sortOrderList":["4730","9459","3034","9011","2625","7139"],
//           // "suppresscolumns" : "entityName,totalOmrSubmittedCount,classWiseOmrCountDataMap.6.studentCount",
//           "downloadtype" : "csv, xls",
// 	  "totaltype": "last",
//           "headers": [
//             {
//               "type": "dynamic",
//               "rows": "2",
//               "fontClass": "thf1"
//             },
//             {
//               "name": "Total Students",
//               "rows": "2",
//               "fontClass": "thf1",
//               "dynamicAppendKey" : "ts1"
//             },
//             {
//               "name": "Total Uploaded OMR's",
//               "rows": "2",
//               "fontClass": "thf1",
//               "dynamicAppendKey" : "ts2"
//             },
//             {
//               "name": "Uploaded Percentage",
//               "rows": "2",
//               "fontClass": "thf1"
//             },
//             {
//               "name": "VI",
//               "rows": "1",
//               "child":[
//                     {
//                       "name": "VI Class",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     },
//                     {
//                       "name": "VI Class Uploaded",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     }]
//             },
//             {
//               "name": "VII",
//               "rows": "1",
//               "child":[
//                     {
//                       "name": "VII Class",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     },
//                     {
//                       "name": "VII Class Uploaded",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     }]
//             },
//             {
//               "name": "VIII",
//               "rows": "1",
//               "child":[
//                     {
//                       "name": "VIII Class",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     },
//                     {
//                       "name": "VIII Class Uploaded",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     }]
//             },
//             {
//               "name": "IX",
//               "rows": "1",
//               "child":[
//                     {
//                       "name": "IX Class",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     },
//                     {
//                       "name": "IX Class Uploaded",
//                       "rows": "1",
//                       "colorClass" : "thc7"
//                     }]
//             }
//           ],
//           "dataKeys": [
//             {
//               "jsonkey": "dynamic",
//               "colorClass": "thc1",
//               "dataType" : "LOCATION"
//             },
//             {
//               "loop": false,
//               "jsonkey": "totalStudentRegistered",
//               "colorClass": "thc1",
//               "dataType" : "NUMBERS"
//             },
//             {
//               "loop": false,
//               "jsonkey": "totalOmrSubmittedCount",
//               "colorClass": "thc1",
//               "dataType" : "NUMBERS"
//             },
//             {
//               "loop": false,
//               "jsonkey": "uploadedPercentage",
//               "colorClass": "thc1",
//               "dataType" : "PERCENTAGEWITHDASH"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&6",
//               "jsonkey": "classWiseOmrCountDataMap.6.studentCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&6",
//               "jsonkey": "classWiseOmrCountDataMap.6.omrUploadCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&7",
//               "jsonkey": "classWiseOmrCountDataMap.7.studentCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&7",
//               "jsonkey": "classWiseOmrCountDataMap.7.omrUploadCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&8",
//               "jsonkey": "classWiseOmrCountDataMap.8.studentCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&8",
//               "jsonkey": "classWiseOmrCountDataMap.8.omrUploadCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //  "mainDataKey": "classWiseOmrCountDataMap&9",
//               "jsonkey": "classWiseOmrCountDataMap.9.omrUploadCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc8"
//             },
//             {
//               "loop": false,
//               //    "mainDataKey": "classWiseOmrCountDataMap&9",
//               "jsonkey": "classWiseOmrCountDataMap.9.studentCount",
//               "dataType" : "NUMBERS",
//               "colorClass": "thc1"
//             }
//           ]
//         },
//
//   "getTable" : {
//                 "sortOrder": false,
//                 "colorClass" : "thc5",
//                 "totalkey": "-1",
//                 "totaltype": "inside",
//                 "headers": [
//                           {
//                             "type": " ",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "Total Students",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "Total Uploaded OMR's",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "Uploaded Percentage",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VI Class",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VI Class Uploaded",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VII Class",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VII Class Uploaded",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VIII Class",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "VIII Class Uploaded",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "IX Class",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "IX Class Uploaded",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "X Class",
//                             "rows": "1"
//                           },
//                           {
//                             "name": "X Class Uploaded",
//                             "rows": "1"
//                           }
//                         ],
//                 "dataKeys": [
//                          {
//                             "jsonkey": "dynamic",
//                             "colorClass": "thc1",
//                             "dataType" : "FINALLOCATION"
//                          },
//                          {
//                           "loop": false,
//                           "jsonkey": "totalStudentRegistered",
//                           "colorClass": "thc1",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                          "loop": false,
//                          "jsonkey": "totalOmrSubmittedCount",
//                          "colorClass": "thc1",
//                          "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                           "jsonkey": "uploadedPercentage",
//                           "colorClass": "thc1",
//                           "dataType" : "PERCENTAGEWITHDASH"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&6",
//                           "jsonkey": "classWiseOmrCountDataMap.6.studentCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&6",
//                           "jsonkey": "classWiseOmrCountDataMap.6.omrUploadCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&7",
//                           "jsonkey": "classWiseOmrCountDataMap.7.studentCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&7",
//                           "jsonkey": "classWiseOmrCountDataMap.7.omrUploadCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&8",
//                           "jsonkey": "classWiseOmrCountDataMap.8.studentCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&8",
//                           "jsonkey": "classWiseOmrCountDataMap.8.omrUploadCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                         //  "mainDataKey": "classWiseOmrCountDataMap&9",
//                           "jsonkey": "classWiseOmrCountDataMap.9.omrUploadCount",
//                           "dataType" : "NUMBERS"
//                         },
//                         {
//                           "loop": false,
//                       //    "mainDataKey": "classWiseOmrCountDataMap&9",
//                           "jsonkey": "classWiseOmrCountDataMap.9.studentCount",
//                           "dataType" : "NUMBERS"
//                         }
//                       ]
//               },
//
// /*EMPTY CONSTANTS ALSO GENERATES TABLE - THE SERVICE USES JSON RESPONSE TO CREATE HEADER OBJECTS*/
//   "cfcTable1":{
//
//         },

/*ALL THE ABOVE FORMATS WILL GENERATE THE TABLE*/

});
