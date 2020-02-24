import { Child } from './child'

export class Superapp {
    super_app_name: string;
    super_app_id: string;
    image_url: string;
    installations: number;
    registered_users: number;
    users_logged_in: number;
    users_never_logged_in: number;
    
    childapps = [];
    app_analytics = [];
    apps : Child[] = [];
}