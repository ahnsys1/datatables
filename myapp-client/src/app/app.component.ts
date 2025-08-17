import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateDirective, TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TranslatePipe, TranslateDirective],
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
