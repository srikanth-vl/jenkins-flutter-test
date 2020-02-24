import { Component, OnInit, Input } from '@angular/core';
import * as Highcharts from 'highcharts';
import { Superapp } from '../shared/superapp';


@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements OnInit {

  constructor() { }
  @Input() superApp: Superapp;

  highcharts = Highcharts;
  apps: object[];
  chartOptions = {
    chart: {
      plotBorderWidth: null,
      plotShadow: false
    },
    legend: {
      align: 'center',
      verticalAlign: 'bottom',
      layout: 'horizontal',
    },
    title: {
      text: 'Usage Analytics'
    },
    tooltip: {
      pointFormat: '<b>{point.percentage:.1f}%</b>'
    },
    plotOptions: {
      pie: {
        shadow: false,
        center: ['50%', '50%'],
        size: '100%',
        innerSize: '55%',
        allowPointSelect: true,
        cursor: 'pointer',
        dataLabels: {
          enabled: false
        },
        showInLegend: true
      }
    },
    series: [{
      type: 'pie',
      data: [
        //  {
        //     name: 'Chrome',
        //     y: 60,
        //     sliced: true,
        //     selected: true
        //  },
      ]
    }],
    //  green: '#50B432', orange: '#ED561B', yellow: '#DDDF00', blue: '#24CBE5', lemon green: '#64E572', 
    colors: ['#24CBE5', '#2A97CE']
  };

  ngOnInit() {
    // console.log(this.superApp);
    let name = this.superApp['super_app_name'];
    this.apps = this.superApp['apps'];

    let installations = { name: 'Installations', y: this.superApp['installations'], sliced: true, selected: true };
    let registered_users = { name: 'Regsitered users', y: this.superApp['registered_users'] };
    let users_logged_in = { name: 'Users Logged in', y: this.superApp['users_logged_in'] };
    let users_never_logged_in = { name: 'Users Never Logged in', y: this.superApp['users_never_logged_in'] };
    this.chartOptions.series[0].data = [];
    // this.chartOptions.series[0].data.push(20);
    // this.chartOptions.series[0].data.push(registered_users);
    this.chartOptions.series[0].data.push(users_logged_in);
    this.chartOptions.series[0].data.push(users_never_logged_in);
  }

}
