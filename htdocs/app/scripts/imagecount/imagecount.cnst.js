angular
.module('imageCount')
.constant('filterConstant', {
     "multipartform" : {
        "formid": "eduapp_000",
        "submissionurl" : "http://uniapp.vassarlabs.com:9003/api/uniapp/multipartformdata",
        "name": "Filter",
        "title": "Event Details",
        "upload": "true",
        "fields": [

               {
                 "label" : "SuperApp",
                 "key" : "superapp",
                 "type" : "dropdown",
                 "listsource" : "fromconfig",
                 "listarray" : ['CADA','APWRIMS','NOBAGDAY','PRRD'],
                 // "mandatory": false,
                 "change" : true,
                 "child" : "app",
                 // "listarray" : {"1":10,"2":20},
                 "sendvalue" : "value"
               },
               {
                 "label" : "App",
                 "type" : "dropdown",
                 "key" : "app",
                 "listsource" : "frombackend",
                 "listarrayurl" : "http://uniapp.vassarlabs.com:9003/api/uniapp/rootconfigdata",
                 "isarray" : false,
                 // "listarray" : [10,20,30,40],
                 "dependent" : true,
                 "parent" : "superapp",
                 "change" : true,
                 // "listarray" : {"1":10,"2":20},
                 "sendvalue" : "value"
               },

             ],
             "buttons": [
               {
                 "key": "submit_btn",
                 "label": "Submit",
               },

             ],
      }
})
.constant('imageCount_table_md',{
  /*DEFINE YOUR TABLETYPE IN THIS WAY*/
  /*EXAMPLE*/
  "imageCountTable": {

    "headers": [{
      "name": "Super Application Id",
    },
    {
      "name": "Super Application Name",
    },
    {
      "name": "Application Id",
    },
    {
      "name": "Application Name",
    },
    {
      "name": "Project Id",
    },
    {
      "name": "ProjectName",
    },
    {
      "name": "Number of Media Files Submitted"
    },
    {
      "name": "Number of Media Files Received",
    },
    {
      "name": "Number of Media Files Relayed"
    },
    {
      "name": "Number of Text Files Relayed",
    }

  ],
  "dataKeys": [{
    "jsonkey": "superAppDetails.entityId",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    "jsonkey": "superAppDetails.entityName",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "applicationDetails.entityId",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "applicationDetails.entityName",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "projectDetails.entityId",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "projectDetails.entityName",
    "colorClass": "thc1",
    "dataType": "CLICKNUMBER"
  },
  {
    //"loop": false,
    "jsonkey": "submittedCount",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "receivedCount",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "mediaFileRelayCount",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },
  {
    //"loop": false,
    "jsonkey": "textDataRelayCount",
    "colorClass": "thc1",
    "dataType": "FINALLOCATION"
  },

]
}
});
