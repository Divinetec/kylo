import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {SimpleDynamicFormDialogComponent} from "../../../shared/dynamic-form/simple-dynamic-form/simple-dynamic-form-dialog.component";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DynamicFormDialogData} from "../../../shared/dynamic-form/simple-dynamic-form/dynamic-form-dialog-data";
import {Template} from "../../../model/template-models";
import {AbstractControl, FormGroup} from "@angular/forms";
import CategoriesService from "../../../services/CategoriesService";
import {Observable} from "rxjs/Observable";
import {Category} from "../../../model/category/category.model";
import {fromPromise} from "rxjs/observable/fromPromise";
import {FeedService} from "../../../services/FeedService";
import * as _ from "underscore";
import {map} from "rxjs/operators/map";
import {of} from "rxjs/observable/of";

export class NewFeedDialogData {

    constructor(public template:Template, public title?:string){}
}


export interface NewFeedDialogResponse{
    template:Template;
    category:Category;
    feedName:string;
    systemFeedName:string;
}

@Component({
    selector:"new-feed-dialog",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/new-feed-dialog/new-feed-dialog.component.html"
})
export class NewFeedDialogComponent implements OnInit, OnDestroy{

    template:Template;
    /**
     * the form to validate
     */
    formGroup:FormGroup;

    title:string;
    /**
     * Contains existing system feed names.
     * key: categorySystemName.feedSystemName, value: feedSystemName
     */
    existingFeedNames:any = {};

    /**
     * Are we populating the feed name list for validation
     * @type {boolean}
     */
    populatingExistingFeedNames:boolean = false;

    private feedService: FeedService;

    constructor(private dialog: MatDialogRef<NewFeedDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: NewFeedDialogData,
                @Inject("FeedService") feedService: FeedService) {
        this.template = data.template;
        this.formGroup = new FormGroup({}, [], [this.validateFeedNameUniqueness.bind(this)]);

        if(data.title){
            this.title = data.title;
        }
        else {
            this.title = "New "+this.template.templateName+" Feed";
        }

        this.feedService = feedService;
        this.populateExistingFeedNames().subscribe();
    }

    ngOnInit() {

    }
    ngOnDestroy(){

    }

    create(){
        let values = this.formGroup.value;
        values.template = this.template;
        let response:NewFeedDialogResponse = <NewFeedDialogResponse>values;
        this.dialog.close(response);
    }
    cancel(){
        this.dialog.close();
    }

    checkDuplicateFeedName(formGroup: FormGroup) {
        return formGroup.hasError('duplicateFeedName');
    }

    validateFeedNameUniqueness(control: AbstractControl) {
        const categoryControl = control.get("category");
        const systemFeedNameControl = control.get("systemFeedName");
        if (categoryControl && systemFeedNameControl && categoryControl.value != null) {
            return this.checkIfFeedNameIsUnique(categoryControl.value.systemName, systemFeedNameControl.value)
                .pipe(
                    map((response: boolean) => {
                            return response ? {duplicateFeedName: true} : null;
                        }
                    )
                )
        } else {
            return of(null);
        }
    }

    checkIfFeedNameIsUnique(categorySystemName: string, feedSystemName: string) : Observable<boolean> {
        return this.populateExistingFeedNames()
            .pipe(
                map((response: any) => {
                    return !!response[this.existingFeedNameKey(categorySystemName, feedSystemName)];
                    }
                )
            )
    }

    populateExistingFeedNames(): Observable<any> {
        if (!this.populatingExistingFeedNames) {
            this.populatingExistingFeedNames = true;
            return fromPromise(this.feedService.getFeedNames())
                .pipe(map((response: any) => {
                    if (response.data != null) {
                        _.each(response.data, (value: string, key: any, list: any) => {
                            //console.log("Found existing category.feed:" + value);
                            var categoryName = value.substr(0, value.indexOf('.'));
                            var feedName = value.substr(value.indexOf('.') + 1);
                            this.existingFeedNames[value] = feedName;
                        });
                    }
                    this.populatingExistingFeedNames = false;
                    return this.existingFeedNames;
                }))
        } else {
            return of(this.existingFeedNames);
        }
    };

    existingFeedNameKey (categorySystemName:string, feedSystemName:string)  {
        return categorySystemName + "." + feedSystemName;
    }
}