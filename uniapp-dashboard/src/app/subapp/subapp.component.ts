import { Component, OnInit, Input } from '@angular/core';
import { Child } from '../shared/child'
import { RouterModule, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'app-subapp',
  templateUrl: './subapp.component.html',
  styleUrls: ['./subapp.component.scss']
})
export class SubappComponent implements OnInit {

  @Input() child: Child;
  collapse: boolean;
appid
  constructor( private route: ActivatedRoute) { }

  ngOnInit() {
    this.collapse = false;
    // console.log(this.child)
    this.route.paramMap.subscribe(params => {
      this.appid=  params.get('appid');    
    });
  }

  collapsable() {
    this.collapse = !(this.collapse);
    // return this.collapse;
  }


}
