(function() {
	angular.module("vassarTable").constant('districtOrderList', {
		"districtList": {
			"names": ['SRIKAKULAM', 'VIZIANAGARAM', 'VISAKHAPATNAM', 'VISHAKAPATNAM', 'EAST GODAVARI', 'WEST GODAVARI', 'KRISHNA', 'GUNTUR', 'PRAKASAM', 'NELLORE', 'CHITTOOR', 'KADAPA', 'Y.S.R KADAPA', 'ANANTHAPUR', 'ANANTAPUR', 'KURNOOL'],
			"ids": ["2811", "2812", "2813", "2814", "2815", "2816", "2817", "2818", "2819", "2823", "2820", "2822", "2821"]
		}
	})
	.constant('tableDefaultConstants', {
		TOTAL_KEY : "-1",
		TOTAL_TYPE : "inside",
		SORT_DISTRICTS_BY : "names",
		SORT_KEY : "entityName",
		SORT_ORDER : false,
		COLOR_CLASS : "thc5",
		NONE : "none"
	})
	.constant('locationDefaultConstants',{
	   JSONKEY : "entityName",
		 DATATYPE : "LOCATION",
		 LOCATION_URL : ["entityId"]
	})
	.service('vassarTableFormatService', vassarTableFormatService);
	vassarTableFormatService.$inject = ['districtOrderList', 'tableDefaultConstants', 'locationDefaultConstants'];
	function vassarTableFormatService(districtOrderList, tableDefaultConstants, locationDefaultConstants) {
		var service = {
			formatTable: formatTable,
			checkLocationConstantForALocation : checkLocationConstantForALocation
		}
		return service;

		/*service function*/
		function formatTable(inputData, tableDataConstants, locationType, currentLocation, districtSortingOn, dynamicHeaderData) {
			var finalTableData = {};
			// console.log(inputData,"inputData");
			var tableObject = {};
			var eachTableObject = {};
			var AllTablesData = [];
			var eachTableTotal = [];
			var tableData = {};
			var tableHeaders = [];
			var tableValues = [];
			var suppressColumnsList = [];

			/*FOR PRE-DEFINED CONSTANTS*/
			if(tableDataConstants && tableDataConstants['headers'] && tableDataConstants['headers'] !== undefined) {
				tableObject = getTableconfig(tableDataConstants);
			}
			/*WHEN CONSTANTS NOT DEFINED*/
			else {
				tableObject = generateTable(tableDataConstants,inputData);
			}
			// console.log("table object ",tableObject);
			//Used for suppressing columns...Maintains a list for columns to be suppressed
			var sortIndex;  //define sort index
			var suppressColumns = tableObject.supresscolumns;
			if(suppressColumns && suppressColumns.length !== ""){
				suppressColumnsList = suppressColumns.split(",");
			}
			var headersConstants = tableObject.headers;
			var sortOrder = tableObject.sortOrder;
			var totalCols = tableObject.totalCols;
			var totalkey = tableObject.totalkey;
			var totalType = tableObject.totalType;
			var dataKeysConstansts = tableObject.dataKeys;
			var dataTypesConstants = angular.copy(tableObject.dataTypes);
			var headerColorClass = tableObject.headerColorClass;
			var sortKey = tableObject.sortKey;
			var sortOrderList = tableObject.sortOrderList;
			var sortDistrictsBy = tableObject.sortDistrictsBy;
			var downloadType = tableObject.downloadtype;
			var location = angular.copy(locationType);
			locationType = checkLocationConstantForALocation(location,currentLocation);
			//to include last row in inputData as total row*/
			if(totalType == "last"){
				var last_table_row = inputData[inputData.length - 1];
				var eachRowDataArray = getDataRowForAServerObject(dataTypesConstants,dataKeysConstansts, last_table_row, currentLocation, locationType, locationType.jsonkey,suppressColumnsList);
				eachTableTotal = eachRowDataArray;
			}
			// if (tableDataConstants['districtSort'] === "true" && locationType.name ==='District'){
			if(sortKey && sortOrderList){
				inputData = SortbyDataList(inputData, sortOrderList, sortKey);
				sortIndex = 50;
				sortOrder = false;
			}
			else{
				if(locationType && locationType.name === 'District') {
					var districtList = getDistrictNamesORids(sortDistrictsBy);
					inputData = SortbyDataList(inputData, districtList, sortKey);
					sortIndex = 50;
					sortOrder = false;
				}
			}
			tableHeaders = getTableHeaders(tableDataConstants,headersConstants, inputData, locationType, dynamicHeaderData);
			dataTypesConstants = setDataTypesForDyanamicFields(headersConstants, dataTypesConstants, locationType, inputData);
			angular.forEach(inputData, function(eachServerData, eachServerDataKey) {
				if(locationType === undefined) {
					locationType = {};
				}
				var eachRowDataArray = getDataRowForAServerObject(dataTypesConstants,dataKeysConstansts, eachServerData, currentLocation, locationType, locationType.jsonkey, suppressColumnsList);
				if(eachServerData.entityId != totalkey) tableValues.push(eachRowDataArray);
				else if(totalType === 'inside') eachTableTotal = eachRowDataArray;
			})
			if(totalType === 'outside') {
				//eachTableTotal = getDataValue(tableDataConstants['totalkeys'], inputData[totalkey], currentLocation, locationType, "displayName");
					eachTableTotal = getDataValue(dataKeysConstansts, tableValues);
			}
			eachTableObject.headers = tableHeaders;
			eachTableObject.recordData = tableValues;
			eachTableObject.sortIndex = sortIndex;
			eachTableObject.totalCols = totalCols;
			eachTableObject.sortOrder = sortOrder;
			eachTableObject.dataTypes = dataTypesConstants;
			eachTableObject.headerColorClass = headerColorClass;
			eachTableObject.downloadType = downloadType;
			eachTableObject.totalData = eachTableTotal;
			// console.log("a ",eachTableObject);
			return eachTableObject;
		}/*end of format function*/

 	/*checks if the location has required parameters*/
		function checkLocationConstantForALocation(locationType,currentLocation){
 				if(locationType){
 					if(!locationType.name)
 						locationType.name = currentLocation.charAt(0).toUpperCase() + currentLocation.slice(1);

 					if(!locationType.jsonkey)
 						locationType.jsonkey = locationDefaultConstants.JSONKEY;

 					if(!locationType.dataType)
 						locationType.dataType = locationDefaultConstants.DATATYPE;

 					if(!locationType.url)
 						locationType.url = locationDefaultConstants.LOCATION_URL;
 				}
 				return locationType;

 	  }


		function calculateSum(datakey, dataArray ) {
			var sum = 0;
			angular.forEach(dataArray, function(dataArrayvalue, dataArraykey) {
					sum = sum + getvalueforrow(datakey, dataArrayvalue);
			});
			return sum;
		}


		function getDataValue(datakeys, tablevalues) {
			var totalarray = [];
			angular.forEach(datakeys, function(datavalue, datakey) {
				var totalObject = {};
				if(datavalue.jsonkey == 'dynamic') {
					var totalObject = {};
					totalObject.value = "Total";
					totalObject.colorClass = datavalue.colorClass;
				} else {
					var totaltypearray = [];
					if(datavalue.totaltype && datavalue.totaltype != undefined) {
						totaltypearray = datavalue.totaltype.split("##");
						if(totaltypearray.length === 1) {
							var totalObject = {};
							var type = totaltypearray[0];
							if(type == "sum") {
								sum = calculateSum(datakey, tablevalues , 0);
								totalObject.value = sum;
								totalObject.colorClass = datavalue.colorClass;
							}
							if(type == "average") {
								sum = calculateSum(datakey, tablevalues , 0);
								totalObject.value = sum / tablevalues.length;
								totalObject.colorClass = datavalue.colorClass;
							}
						  if(type == "none") {
								eachtabletotalobject.value = "-";
								eachtabletotalobject.colorClass = datavalue.colorClass;
							}
						} else {
							var totalObject = [];
							angular.forEach(totaltypearray, function(totaltypevalue, totaltypekey) {
								var eachtabletotalobject = {};
								sum = 0;
								angular.forEach(tablevalues, function(tablevalue, tablekey) {
									if(totaltypevalue == "sum") {
										angular.forEach(tablevalue, function(innertablevalue, innertablekey) {
											if(innertablekey == datakey) {
												sum = sum + getvalueforrow(totaltypekey, innertablevalue)
											}
										});
										//sum = calculateSum(totaltypekey,tablevalue,datakey);
										eachtabletotalobject.value = sum;
										eachtabletotalobject.colorClass = datavalue.colorClass;
									} else if(totaltypevalue == "average") {
										angular.forEach(tablevalue, function(innertablevalue, innertablekey) {
											if(innertablekey == datakey) {
												sum = sum + getvalueforrow(totaltypekey, innertablevalue);
											}
										});
										//sum = calculateSum(totaltypekey,tablevalue,datakey);
										eachtabletotalobject.value = sum / tablevalues.length;
										eachtabletotalobject.colorClass = datavalue.colorClass;
									} else if(totaltypevalue == "none") {
										eachtabletotalobject.value = "-";
										eachtabletotalobject.colorClass = datavalue.colorClass;
									}
								});
								totalObject.push(eachtabletotalobject);


							});
						}
					} else if(datavalue.totaltype === undefined) {
						var totalObject = {};
						totalObject.value = "-";
						totalObject.colorClass = datavalue.colorClass;
					}
				}
				// console.log("tableObject is-------", totalObject);
				totalarray.push(totalObject);
			});
			return totalarray;
		}

		function getvalueforrow(recordkey, tablevalues) {
			angular.forEach(tablevalues, function(tabvalue, key) {
				if(recordkey == key) {
					value = tabvalue.value;
					if(!angular.isNumber(value)) {
						value = 0;
					}
				}
			});
			return value;
		}





		/* function to generate the table with json*/
		function generateTable(jsondata,inputData) {
			var generateTableObject = {};
			var dataKeys = [];
			var dataTypes = [];
			var res = {};
			if (jsondata && jsondata['names'] != undefined){
				res = flattenObject(jsondata['names']);
				dataTypes = angular.copy(jsondata['dataTypes']);
			}
			else {
				res = flattenObject(inputData[0]);
				angular.forEach(res ,function(key,value){
					dataTypes.push("NUMBERS");
				});
			}

			function flattenObject(ob) {
				var toReturn = {};
				for(var i in ob) {
					if(!ob.hasOwnProperty(i)) continue;
					if((typeof ob[i]) == 'object') {
						var flatObject = flattenObject(ob[i]);
						for(var x in flatObject) {
							if(!flatObject.hasOwnProperty(x)) continue;
							toReturn[i + '&' + x] = flatObject[x];
						}
					} else {
						toReturn[i] = ob[i];
					}
				}
				return toReturn;
			};
			angular.forEach(res, function(resvalue, reskey) {
				var newobject = {};
				if(reskey.indexOf('&') > -1) {
					var resarray = reskey.split("&");
					newobject.jsonkey = resarray.slice(-1)[0];
					//newobject.jsonkey = resarray.slice(1).slice(-2) .join("");
					var n = reskey.lastIndexOf("&");
					newobject.mainDataKey = reskey.substring(0, n);
					newobject.loop = false;
				} else {
					newobject.jsonkey = reskey;
					// if(newobject.jsonkey == "entityName") {
					// 	newobject.jsonkey = "dynamic";
					// }
					if(newobject.jsonkey != "dynamic") {
						newobject.loop = false;
					}
				}
				dataKeys.push(newobject);
			});
			var allHeaders = [];
			angular.forEach(dataKeys, function(value, key) {
				var headerObject = {};
				if(value.jsonkey == "dynamic") {
					headerObject.type = "dynamic";
					headerObject.rows = 1;
				}
				if(value.mainDataKey && value.mainDataKey !== undefined) {
					var resarray = value.mainDataKey.split("&");
					/*FORMING HEADER NAME*/
					headerObject.name = resarray.slice(-1)[0] + " " + value.jsonkey;
				} else {
					headerObject.name = value.jsonkey;
					headerObject.rows = 1;
				}
				allHeaders.push(headerObject);
			});
			allHeaders = convertHeadersToRowFormat(allHeaders);
			generateTableObject.dataTypes = dataTypes;
			generateTableObject.headerColorClass = tableDefaultConstants.COLOR_CLASS;
			generateTableObject.headers = allHeaders;
			generateTableObject.sortOrder = tableDefaultConstants.SORT_ORDER;
			generateTableObject.totalCols = calculateTotalCols(allHeaders);
			generateTableObject.dataKeys = dataKeys;
			generateTableObject.totalkey = tableDefaultConstants.TOTAL_KEY;
			generateTableObject.totalType = tableDefaultConstants.TOTAL_TYPE;
			return generateTableObject;
		} /*end of function*/



		/*returns the pre-defined table*/
		function getTableconfig(tableDataConstants){
			// console.log("table data constants ",tableDataConstants);
			var table = {};
			table.headers = getHeaders(tableDataConstants);
			table.sortOrder = tableDataConstants['sortOrder'] || tableDefaultConstants.SORT_ORDER;
			table.totalCols = calculateTotalCols(angular.copy(table.headers));
			table.totalkey = tableDataConstants['totalkey'] || tableDefaultConstants.TOTAL_KEY;
			table.totalType = tableDataConstants['totaltype'] || tableDefaultConstants.TOTAL_TYPE;
			table.sortDistrictsBy = tableDataConstants['sortDistrictsBy'] || tableDefaultConstants.SORT_DISTRICTS_BY;
			table.dataKeys = tableDataConstants['dataKeys'];
			table.sortKey = tableDataConstants['sortKey'] || tableDefaultConstants.SORT_KEY;
			table.supresscolumns = tableDataConstants['suppresscolumns'];
			if(table.sortKey && tableDataConstants['sortOrderList']){
				table.sortOrderList = tableDataConstants['sortOrderList'];
			}
			table.dataTypes = getdataTypesConstants(angular.copy(table.dataKeys))
			table.headerColorClass = tableDataConstants['colorClass'] || tableDefaultConstants.COLOR_CLASS;
			console.log("s ",tableDataConstants['downloadtype']);
			table.downloadtype = tableDataConstants['downloadtype'] || tableDefaultConstants.NONE;
			return table;
		}



		/*returns total number of columns in the table*/
		function calculateTotalCols(headers) {
			var headerfirstrow = headers[0]
			var totalCols = 0;
			if(headerfirstrow && headerfirstrow !== undefined) {
				angular.forEach(headerfirstrow, function(headerfirstrowvalue) {
					totalCols += headerfirstrowvalue.cols;
				});
			}
			return totalCols;
		} /*end of function*/


		/*formatter function for headers*/
		function convertHeadersToRowFormat(headersConstantData) {
			var headers = {};
			var headersModifyData = calculateColsAndSortIndexEachHeader(headersConstantData);
			headers = getHeaderModefiedData(headersModifyData);
			return headers;
		} /*end of function */


		/*modifying the headers to the required format*/
		function getHeaderModefiedData(headersModifyData) {
			var headers = {};
			angular.forEach(headersModifyData, function(headerData, key) {
				convertHeaderRows(headerData, 0)
			});
			function convertHeaderRows(headerData, level) {
				if(headers[level] === undefined) headers[level] = [];
				if(headerData.child && headerData.child.length > 0) {
					for(var i = 0; i < headerData.child.length; i++) {
						convertHeaderRows(headerData.child[i], level + 1);
					}
				}
				var headerCopy = angular.copy(headerData);
				delete headerCopy.child;
				headers[level].push(headerCopy);
			}
			return headers;
		} /*end of function */


		/*function to calculate totalrows for table headers*/
		function calculateTotalRows(headersConstantData){
							var totalRows = 0;
							angular.forEach(headersConstantData,function(headerData,key){
								recursion(headerData,0);
							});
							function recursion (data, level) {
							    if (data.child  && data.child.length > 0) {
							        for (var i = 0; i < data.child.length; i++) {
							            recursion(data.child[i], level + 1);
							        }
							    } else {
							        if (level > totalRows) {
							            totalRows = level;
							        }
							    }
						}
					  return totalRows+1;
		}/*end of function*/

		/*returns the colspan and sortIndex for each header*/
		function calculateColsAndSortIndexEachHeader(headersConstantData) {
			var startSortIndex = 0;
			var rows_count = calculateTotalRows(headersConstantData);
			angular.forEach(headersConstantData, function(headerData, key) {
				calculateRowspan(headerData,rows_count);
				calculateColspan(headerData);
				calculatesortIndex(headerData);
			}); /*end of function */


			/*function to calculate the rowspan*/
			function calculateRowspan(headerData,level){
						 if(headerData.child && headerData.child.length > 0) {
							 level = level - 1;
							 cur = level;
							 for(var i = 0; i < headerData.child.length; i++) {
								 calculateRowspan(headerData.child[i],level);
							 }
						 } else {
							  headerData.rows = level;
						 }
			}/*end of function */


			/*function to calculate the colspan*/
			function calculateColspan(headerData) {
				headerData.cols = 0;
				if(headerData.child && headerData.child.length > 0) {
					for(var i = 0; i < headerData.child.length; i++) {
						calculateColspan(headerData.child[i])
						headerData.cols += headerData.child[i].cols;
					}
				} else {
					return headerData.cols++;
				}
			} /*end of function */


			/*function to calculate the sortIndex*/
			function calculatesortIndex(headerData) {
				if(headerData.child && headerData.child.length > 0) {
					for(var i = 0; i < headerData.child.length; i++) {
						calculatesortIndex(headerData.child[i]);
					}
				} else {
					headerData.sortIndex = startSortIndex;
					startSortIndex++;
				}
			}
			return headersConstantData;
		} /*end of function */



		function getdataTypesConstants(dataKeysConstansts) {
			var dataTypesConstants = [];
			angular.forEach(dataKeysConstansts, function(dataKeysvalue, key) {
				dataTypesConstants.push(dataKeysvalue.dataType);
			})
			return dataTypesConstants;
		}


		/*returns the final headers for table*/
		function getHeaders(tableDataConstants) {
			var reqHeaders = [];
			reqHeaders = convertHeadersToRowFormat(tableDataConstants['headers']);
			return reqHeaders;
		}


		function getDataRowForAServerObject(dataTypesConstants,dataKeysConstansts, dataObject, currentLocation, locationType, dyanamicKey, suppressColumnsList) {
			var eachRowDataArray = [];
			angular.forEach(dataKeysConstansts, function(eachDataKey) {
				var key = eachDataKey.jsonkey;
				// console.log("suppressColumns.contai ",suppressColumnsList.includes(key), key);
				var serverData = dataObject;
				if(eachDataKey.jsonkey === "dynamic") key = dyanamicKey;
				if(eachDataKey.mainDataKey !== undefined) {
					serverData = getDataObjectForKey(dataObject, eachDataKey.mainDataKey);
				}
				if(serverData === null) {
					serverData = {};
				}
				var dataKeysArray = key.split("##");
				// console.log("dataKeysArray IS--------",dataKeysArray);
				var colorClass = eachDataKey.colorClass;
				if(eachDataKey.loop === true) {
					angular.forEach(serverData, function(value, key) {
						eachRowDataArray.push(getDataObjectsForKeys(value, dataKeysArray, currentLocation, locationType, colorClass, eachDataKey.jsonkey, eachDataKey.status,eachDataKey.dataType, suppressColumnsList));
					})
				} else {
					eachRowDataArray.push(getDataObjectsForKeys(serverData, dataKeysArray, currentLocation, locationType, colorClass, eachDataKey.jsonkey, eachDataKey.status , eachDataKey.dataType, suppressColumnsList));
				}
			})
			return eachRowDataArray;
		}

		function getDataObjectsForKeys(indata, dataKeys, currentLocation, locationType, colorClass, jsonkey, status , dataType, suppressColumnsList) {
			var suppress = false;
			if(dataKeys.length === 1) {
				var dataObject = {};
				dataObject.value = getDefault(indata[dataKeys]);
				dataObject.colorClass = indata.colorClass || colorClass;
				dataObject.status = status;
				//If there is a suppress column list present, check if the data key is present in the list,
				// assign a key suppress and set it to true...html won't render cells for which suppress is true
				if(suppressColumnsList){
					if(suppressColumnsList.indexOf(dataKeys[0]) > -1){
						suppress = true;
					}
				}
				if(jsonkey === "dynamic"  ) {
					var newvalue = indata;
					if(indata instanceof Array) newvalue = indata[0] || indata[1];
					dataObject.value = newvalue[dataKeys];
					dataObject.child = locationType.child;
					dataObject.parent = currentLocation;
					dataObject.location = getURLFromLocationType(locationType, newvalue);
				}
				if(suppress){
					dataObject.suppress = true;
				}
				return dataObject;
			} else {
				var dataObjectArray = [];
				angular.forEach(dataKeys, function(datakey) {
					//same as above
					if(suppressColumnsList){
						if(suppressColumnsList.indexOf(datakey) > -1){
							suppress = true;
						}
					}
					var dataObject = {};
					dataObject.colorClass = indata.colorClass || colorClass;
					dataObject.status = status;
					dataObject.value = getDefault(indata[datakey]);
					if(datakey.jsonkey === "dynamic") {
						var newvalue = serverData;
						if(indata instanceof Array) newvalue = indata[0] || indata[1];
						dataObject.value = newvalue[datakey];
						dataObject.child = locationType.child;
						dataObject.parent = currentLocation;
						dataObject.location = getURLFromLocationType(locationType, newvalue);
					}
					dataObject.suppress = true;
					dataObjectArray.push(dataObject);

				});
				return dataObjectArray;
			}
		}

		function getURLFromLocationType(locationType, data) {
			var locationData = [];
			angular.forEach(locationType['url'], function(value, key) {
				locationData.push(data[value]);
			});
			return locationData.join("##");
		}

		function getDefault(value) {
			if(value === null || value === undefined) return "-";
			return value;
		}

		function setDataTypesForDyanamicFields(headersConstants, dataTypesConstants, locationType, data) {
			angular.forEach(headersConstants, function(eachRowheaderValue, eachRowHeaderKey) {
				var index = 0;
				angular.forEach(eachRowheaderValue, function(eachHeaderValue, eachHeaderKey) {
					if(eachHeaderValue.type === "dynamic" && eachHeaderValue.jsonkey === undefined) {
						dataTypesConstants[index] = locationType['dataType'];
					}
					if(eachHeaderValue.type === "dynamic" && eachHeaderValue.jsonkey !== undefined) {
						var headerObjectData = getFirstobjectData(data);
						var headerDataForKey = getDataObjectForKey(headerObjectData, eachHeaderValue.mainDataKey);
						if(eachHeaderValue.loop === true) {
							angular.forEach(headerDataForKey, function(value) {
								dataTypesConstants.push(dataTypesConstants[index]);
							})
						}
					}
					index++;
				});
			});
			return dataTypesConstants;
		}


		function getTableHeaders(tableDataConstants,headersConstants, data, locationType, dynamicHeaderData) {
			var AllHeaders = [];
			angular.forEach(headersConstants, function(eachRowheaderValue, eachRowHeaderKey) {
				var eachRowHeaderArray = [];
				angular.forEach(eachRowheaderValue, function(eachHeaderValue, eachHeaderKey) {
					var headerObject = {};
					if(eachHeaderValue.type === "dynamic" && eachHeaderValue.jsonkey === undefined || eachHeaderValue.type === " " && eachHeaderValue.jsonkey === undefined) {
						// console.log("loc name ",locationType['name']);
						headerObject.name = locationType['name'];
					} else if(eachHeaderValue.type === "dynamic" && eachHeaderValue.jsonkey !== undefined) {
						var headerObjectData = getFirstobjectData(data);
						var headerDataForKey = getDataObjectForKey(headerObjectData, eachHeaderValue.mainDataKey);
						var headerJsonKeys = eachHeaderValue.jsonkey.split('##');
						if(eachHeaderValue.loop === true) {
							angular.forEach(headerDataForKey, function(headerData, key) {
								var loopHeaderObject = {};
								loopHeaderObject = getHeaderForKeys(eachHeaderValue, headerData, headerJsonKeys);
								eachRowHeaderArray.push(loopHeaderObject);
							})
						} else {
							headerObject = getHeaderForKeys(eachHeaderValue, headerData, headerjsonkeys);
						}
					} else {
						headerObject.name = eachHeaderValue.name;
						if(eachHeaderValue.static !== undefined) headerObject.name = eachHeaderValue.static + eachHeaderValue.name;
					}

					//Appends dynamic values to header labels
					if(eachHeaderValue.dynamicAppendKey){
						if(dynamicHeaderData){
								headerObject.name = headerObject.name + " " + dynamicHeaderData[eachHeaderValue.dynamicAppendKey];
						}
					}
					//headerObject.colorClass = eachHeaderValue.colorClass || tableDataConstants['colorClass'];
					if(eachHeaderValue.colorClass && eachHeaderValue.colorClass != undefined){
						headerObject.colorClass = eachHeaderValue.colorClass;
					}
					else{
						headerObject.colorClass = tableDataConstants['colorClass'];
					}
					headerObject.cols = eachHeaderValue.cols;
					headerObject.rows = eachHeaderValue.rows;
					headerObject.sortIndex = eachHeaderValue.sortIndex;
					//fontClass
					if(eachHeaderValue.fontClass && eachHeaderValue.fontClass != undefined){
						headerObject.fontClass = eachHeaderValue.fontClass;
					}

					if(eachHeaderValue.loop !== true) {
						eachRowHeaderArray.push(headerObject);
					}
				});
				AllHeaders.push(eachRowHeaderArray);
			});
			return AllHeaders;
		}

		function getFirstobjectData(data) {
			if(data !== null) {
				var keys = Object.keys(data);
				var firstkey = keys[0];
				return data[firstkey];
			}
			return data;
		}

		function getHeaderForKeys(headerConstantData, data, keys) {
			var headerObject = {};
			if(keys.length == 1) {
				headerObject.name = data[keys];
			} else {
				var headerObjectArray = [];
				angular.forEach(keys, function(value) {
					headerObjectArray.push(data[value])
				})
				headerObject.name = headerObjectArray.join(" - ");
			}
			headerObject.colorClass = headerConstantData.colorClass;
			if(headerConstantData.static !== undefined) headerObject.name = headerConstantData.static + headerObjects.name;
			return headerObject;
		}

		function getDataObjectForKey(dataObject, key) {
			var keyArray = key.split('&');
			if(keyArray.length == 1) {
				if(dataObject != undefined && dataObject[key] != null) return dataObject[key];
				else return null;
			}
			else {
				var dataForKey = dataObject;
				angular.forEach(keyArray, function(key) {
					if(dataForKey !== null && dataForKey !== undefined) {
						if(dataForKey[key] != undefined) {
							dataForKey = dataForKey[key];
						} else {
							dataForKey[key] = {};
						}
					} else {
						dataForKey = {};
					}
				})
				return dataForKey;
			}
		}

		function getDistrictNamesORids(districtSortingOn) {
			return districtOrderList['districtList'][districtSortingOn];
		}

		function SortbyDataList(data, sortDataList, keyname) {
		  var sortList = angular.copy(sortDataList);
		  var sortData = [];
		  var sortkeysbyDatalist = [];
		  var allkeys = [];
		  angular.forEach(data, function(val) {
		    var keyData = val[keyname] || val.districtName;
		    if(allkeys.indexOf(keyData) == -1) allkeys.push(keyData);
		  });
		  sortkeysbyDatalist = allkeys.map(function(item) {
		    var n = sortList.indexOf(item.toString());
		    sortList[n] = '';
		    return [n, item];
		  }).sort(sortbyFirstElement).map(function(j) {
		    return j[1]
		  });

		  function sortbyFirstElement(a, b) {
		    if(a[0] < b[0]) return -1;
		    if(a[0] > b[0]) return 1;
		    return 0;
		  }
		  sortkeysbyDatalist = sortkeysbyDatalist.filter(function(elem, index, self) {
		    return index == self.indexOf(elem);
		  })
		  angular.forEach(sortkeysbyDatalist, function(value, key) {
		    angular.forEach(data, function(value1, key1) {
		      if(value1[keyname] === value || value1.districtName === value) {
		        sortData.push(value1);
		      }
		    });
		  });
		  return sortData;
		}
	}
})();
