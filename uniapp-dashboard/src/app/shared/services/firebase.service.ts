import { Injectable } from '@angular/core';
import { AngularFirestore ,AngularFirestoreCollection} from '@angular/fire/firestore';
import { AngularFireDatabase } from '@angular/fire/database';
import { error, log } from 'util';

@Injectable({
  providedIn: 'root'
})
export class FirebaseService {

  constructor( public firebasedb:AngularFireDatabase ) { }

  getUsers(){
    this.firebasedb.database.ref("PrrdUser/User").once('value').then(function(snapshot) {
     var username = snapshot.val() || 'Anonymous';
     console.log(username)
   });
  }
}
