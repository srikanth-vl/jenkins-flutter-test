angular.module('ngExportExcel', [])
    .directive('exportExcel', ['$document', '$timeout', function($document, $timeout) {
        return {
            restrict: 'AC',
            scope: {
                filename: '@filename',
                exportDivId: '@exportDivId',
                downloadtype: '@downloadtype'
            },
            link: function(scope, element, attrs) {
                var downloadTypeArray = scope.downloadtype.split(",");
                var exportDivId = '#' + scope.exportDivId;
                var downloadtype = scope.downloadtype;
                var xlsfilename = scope.filename || "file";
                var csvfilename = scope.filename || "file";
                if(downloadtype.indexOf("csv") !== -1){
                  csvfilename += '.csv';
                } if(downloadtype.indexOf("xls") !== -1){
                  xlsfilename += '.xls';
                }

                function setExcelData() {
                    var excelData = '';


                    $(exportDivId).find('table:visible').each(function() {


                        excelData = excelData + "<table>";
                        $(this).find('thead:visible').find('tr').each(function() {


                            if ($(this).css('display') != 'none' || $(this).attr("add-to-excel") === 'true'  ) {
                                excelData = excelData + "<tr>";
                                if($(this).attr("add-to-excel") === 'true')
                                  $(this).removeAttr("style");

                                $(this).filter(":visible").find('th').each(function(index, data) {


                                    if ($(this).css('display') != 'none') {
                                        var colSpan = ($(this).attr("colspan") > 0) ? $(this).attr("colspan") : 1;
                                        var rowSpan = ($(this).attr("rowspan") > 0) ? $(this).attr("rowspan") : 1;


                                        excelData += "<th colspan='" + colSpan + "' rowspan='" + rowSpan + "'  >" + unescape(removeImgFromhtml($(this).html())).trim() + "</th>";


                                    }
                                });

                                excelData = excelData + '</tr>';

                                function removeImgFromhtml(data){
                                  return data.replace(/<img[^>]*>/g,"");
                                }

                                if($(this).attr("add-to-excel") === 'true')
                                  $(this).css("display","none");
                            }
                        });


                        $(this).find('tbody:visible').find('tr').each(function() {

                            if ($(this).css('display') != 'none' || $(this).attr("add-to-excel") === 'true' ) {
                                excelData = excelData + "<tr>";
                                $(this).filter(':visible').find('td').each(function(index, data) {
                                    if ($(this).css('display') != 'none') {
                                        var colSpan = ($(this).attr("colspan") > 0) ? $(this).attr("colspan") : 1;
                                        var rowSpan = ($(this).attr("rowspan") > 0) ? $(this).attr("rowspan") : 1;
                                        var data = $(this).html();
                                        data = data.replace('<a', '<span').replace('a>', 'span>')
                                        excelData += "<td colspan='" + colSpan + "' rowspan='" + rowSpan + "' >" + unescape(data).trim() + "</td>";
                                    }

                                });

                                excelData = excelData + '</tr>';
                            }
                        });


                        excelData = excelData + '<tr><td></td></tr></table>';

                    })


                    var excelFile = "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:x='urn:schemas-microsoft-com:office:excel' xmlns='http://www.w3.org/TR/REC-html40'>";
                    excelFile += "<head>";
                    excelFile += "<!--[if gte mso 9]>";
                    excelFile += "<xml>";
                    excelFile += "<x:ExcelWorkbook>";
                    excelFile += "<x:ExcelWorksheets>";
                    excelFile += "<x:ExcelWorksheet>";
                    excelFile += "<x:Name>";
                    excelFile += xlsfilename;
                    excelFile += "</x:Name>";
                    excelFile += "<x:WorksheetOptions>";
                    excelFile += "<x:DisplayGridlines/>";
                    excelFile += "</x:WorksheetOptions>";
                    excelFile += "</x:ExcelWorksheet>";
                    excelFile += "</x:ExcelWorksheets>";
                    excelFile += "</x:ExcelWorkbook>";
                    excelFile += "</xml>";
                    excelFile += "<![endif]-->";
                    excelFile += "</head>";
                    excelFile += "<body>";
                    excelFile += excelData;
                    excelFile += "</body>";
                    excelFile += "</html>";

                    return excelFile;


                }

                function exportTableToCSV(filename, doc, excelData) {
                  var csv = [];
                  $(exportDivId).find('table:visible').each(function(index, data) {
                    var rows = data.getElementsByTagName("tr");
                    var rowArray = [];
                    for (var i = 0; i < rows.length; i++) {
                      var row = [], cols = rows[i].querySelectorAll("td, th");
                      for (var j = 0; j < cols.length; j++){
                        row.push(cols[j].innerText.replace("\n",""));
                      }
                      row = row.join(",");
                      rowArray.push(row);
                    }
                    csv.push(rowArray.join("\n"));
                    csv = csv.join("")
                    downloadCSV(csv, filename);
                  });
                }

                function downloadCSV(csv, filename) {
                    var csvFile;
                    var downloadLink;

                    // CSV file
                    csvFile = new Blob([csv], {type: "text/csv"});

                    // Download link
                    downloadLink = document.createElement("a");
                    // File name
                    downloadLink.download = filename;

                    // Create a link to the file
                    downloadLink.href = window.URL.createObjectURL(csvFile);

                    // Hide download link
                    downloadLink.style.display = "none";

                    // Add the link to DOM
                    document.body.appendChild(downloadLink);

                    // Click download link
                    downloadLink.click();
                }

                function doClick(exceldata) {
                  // console.log("ecc ",exceldata);
                    var blob = new Blob([exceldata], {
                        type: "text/html"
                    });
                    if (window.navigator.msSaveOrOpenBlob) {
                        window.navigator.msSaveBlob(blob, filename);
                    } else {
                        var downloadContainer = angular.element('<div data-tap-disabled="true"><a></a></div>');
                        var downloadLink = angular.element(downloadContainer.children()[0]);
                        downloadLink.attr('href', window.URL.createObjectURL(blob));
                        downloadLink.attr('download', xlsfilename);
                        downloadLink.attr('target', '_blank');
                        $document.find('body').append(downloadContainer);
                        $timeout(function() {
                            downloadLink[0].click();
                            downloadContainer.remove();
                        }, null);
                    }
                }

                element.bind('click', function(e) {
                    var exceldata = setExcelData();
                    if(downloadtype.indexOf("csv") !== -1){
                      exportTableToCSV(csvfilename,exportDivId,exceldata);
                    }
                    if(downloadtype.indexOf("xls") !== -1){
                      doClick(exceldata);
                    }
                    scope.$apply();
                });
            }
        }
    }]);
