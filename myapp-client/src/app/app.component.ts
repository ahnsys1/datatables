import { Component, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import {
  TranslatePipe,
  TranslateDirective
} from "@ngx-translate/core";
import { BrowserModule } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, TranslateModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Angular 19';

  constructor(private translate: TranslateService) {
    this.translate.addLangs(['cs', 'en']);
    this.translate.setDefaultLang('en');
    this.translate.use('en');
  }
}
