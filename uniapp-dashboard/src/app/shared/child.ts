export class Child {
    app_id: string;
    app_name: string;
    parent_id: string;
    image_url: string;
    total_projects: number;
    assigned_projects: number;
    total_submissions: number;
    childApps : Child[] =  [];
    child: Child[] = [];
    attribute_heirarchy = [];
}