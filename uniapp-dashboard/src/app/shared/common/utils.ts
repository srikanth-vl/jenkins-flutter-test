export class Utils {
  static getModelTitlesToString(models, titleProperty) {
    let msg = "";
    switch (models.length) {
      case 1:
        msg = models[0][titleProperty];
        break;
      case 2:
        msg = (models.map(m => m[titleProperty])).join(" and ");
        break;
      case 0:
        msg = "";
        break;
      default:
        let n = models.length;
        let m = [models[0], models[1]];
        msg = m.map(m => m[titleProperty]).join(", ");
        msg += " and " + (n - 2) + " more";
    }
    return msg;
  }
  static dialogPopupMessage(entityType, entities, action) {
    let dialogMessageBox = {
      title: '',
      entityList: Utils.getModelTitlesToString(entities, 'title'),
      message: '',
      action: action
    }
    switch (entityType) {
      case 'project':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete Project'
              dialogMessageBox.message = 'will be removed.'
              break;
            case 'archive':
              dialogMessageBox.title = 'Are you sure?'
              dialogMessageBox.message = 'will be archived.'
              dialogMessageBox.action = 'add'
              break;
            case 'active':
              dialogMessageBox.title = 'Are you sure?'
              dialogMessageBox.message = 'will be active.'
              dialogMessageBox.action = 'add'
              break;
            case 'delete user':
              dialogMessageBox.title = 'Remove User'
              dialogMessageBox.message = 'will be removed.'
              dialogMessageBox.action = 'delete'
              break;
            case 'delete asset':
              dialogMessageBox.title = 'Remove Asset'
              dialogMessageBox.message = 'will be removed.'
              dialogMessageBox.action = 'delete'
              break;
          }
        }
        break;
      case 'asset':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete Asset?'
              dialogMessageBox.message = 'will be deleted.'
              break;
            case 'share':
              dialogMessageBox.title = 'Share Asset'
              dialogMessageBox.entityList = entities[0].metadataUrl
              dialogMessageBox.message = ''
              dialogMessageBox.action = 'copy'
              break;
          }
        } break;
      case 'collection':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete Collection?'
              dialogMessageBox.message = 'will be removed from Collections.'
              break;
            case 'delete user':
              dialogMessageBox.title = 'Remove User'
              dialogMessageBox.message = 'will be removed.'
              dialogMessageBox.action = 'delete'
              break;
            case 'delete asset':
              dialogMessageBox.title = 'Remove Asset'
              dialogMessageBox.message = 'will be removed.'
              dialogMessageBox.action = 'delete'
              break
          }
        }
        break;
      case 'guideline':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete Guideline?'
              dialogMessageBox.message = 'will be removed from Guidelines.'
              break;
            case 'share':
              dialogMessageBox.title = 'Share Guideline'
              dialogMessageBox.entityList = entities[0].path
              dialogMessageBox.message = ''
              dialogMessageBox.action = 'copy'
              break;
          }
        }
        break;
      case 'template':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete Template?'
              dialogMessageBox.message = 'will be removed from Guidelines.'
              break;
            case 'share':
              dialogMessageBox.title = 'Share Template'
              dialogMessageBox.entityList = entities[0].path
              dialogMessageBox.message = ''
              dialogMessageBox.action = 'copy'
              break;
            case 'saveConfiguration':
              dialogMessageBox.title = 'Discard unsaved changes? '
              dialogMessageBox.entityList = '';
              dialogMessageBox.message = ''
              dialogMessageBox.action = 'discard'
              break;
          }
        }
        break;
      case 'user':
        {
          switch (action) {
            case 'delete':
              dialogMessageBox.title = 'Delete User'
              dialogMessageBox.message = 'will be removed.'
              dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'label')
              break;
          }
        } break;
      case 'asset group': {
        switch (action) {
          case 'delete':
            dialogMessageBox.title = 'Delete Asset-Group'
            dialogMessageBox.message = 'will be removed.'
            dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'name')
            break;
          case 'update':
            dialogMessageBox.title = 'Edit Asset Group '
            dialogMessageBox.message = 'Asset Group Title'
            dialogMessageBox.action = 'save'
            break;
          case 'create':
            dialogMessageBox.title = 'Create Asset Group'
            dialogMessageBox.message = 'Enter Asset Group Title'
            dialogMessageBox.action = 'create'
            dialogMessageBox.entityList = ''
            break;
        }
      } break;
      case 'category': {
        switch (action) {
          case 'delete':
            dialogMessageBox.title = 'Delete Category'
            dialogMessageBox.message = 'will be removed.'
            dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'name')
            break;
          case 'update':
            dialogMessageBox.title = 'Edit Category'
            dialogMessageBox.message = 'Category Name'
            dialogMessageBox.action = 'save'
            dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'name')
            break;
          case 'create':
            dialogMessageBox.title = 'Create Category'
            dialogMessageBox.message = 'Enter Category Name'
            dialogMessageBox.action = 'create'
            dialogMessageBox.entityList = ''
            break;
        }
      } break;
      case 'sub category': {
        switch (action) {
          case 'delete':
            dialogMessageBox.title = 'Delete Sub Category'
            dialogMessageBox.message = 'will be removed.'
            dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'name')
            break;
          case 'update':
            dialogMessageBox.title = 'Edit Sub Category'
            dialogMessageBox.message = 'Sub Category Name'
            dialogMessageBox.action = 'save'
            dialogMessageBox.entityList = Utils.getModelTitlesToString(entities, 'name')
            break;
          case 'create':
            dialogMessageBox.title = 'Create Sub Category'
            dialogMessageBox.message = 'Enter Sub Category Name'
            dialogMessageBox.action = 'create'
            dialogMessageBox.entityList = ''
            break;
        }
      } break;
    }
    return dialogMessageBox;
  }
  static snackbarMessage(entityType, action, entities = [], response, relatedTo) {
    let message = '';
    let content = ''
    switch (entityType) {
      case 'project':
        {
          switch (action) {
            case 'create':
              message = response == '200' ? entities[0].title + ' created' : 'Project creation failed';
              break;
            case 'update':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' updated' : entities[0].title + ' update failed';
              break;
            case 'delete':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' deleted' : 'Project delete failed';
              else message = response == '200' ? entities.length + ' projects deleted' : 'Projects delete failed';
              break;
            case 'archive':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' archived' : 'Project archive failed';
              else message = response == '200' ? entities.length + ' projects archived' : 'Projects archive failed';

              break;
            case 'active':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' is active' : 'Project activation failed';
              else message = entities.length + (response == '200' ? ' projects are active' : 'projects cannot be made active');
              break;
            case 'remove user':
              if (entities.length == 1)
                message = response == '200' ? entities[0].name + ' removed' : 'Failed to remove user';
              break;
            case 'add user':
              if (entities.length == 1)
                message = response == '200' ? 'Added ' + entities[0].name + ' to ' + relatedTo.title : 'Failed to add ' + entities[0].name + ' to ' + relatedTo.title;
              else
                message = response == '200' ? 'Added ' + entities.length + ' users to ' + relatedTo.title : 'Failed to add users to project';
              break;
            case 'remove asset':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' removed from ' + relatedTo.title : entities[0].title + ' remove failed';
              break;
          }
        }
        break;
      case 'asset':
        {
          switch (action) {
            case 'update':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' updated.' : entities[0].title + ' update failed.';
              else message = entities.length + (response == '200' ? ' assets updated.' : ' assets update failed.');
              break;
            case 'delete':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' deleted.' : entities[0].title + ' delete failed.';
              else message = entities.length + (response == '200' ? ' assets deleted.' : ' assets delete failed.');
              break;
            case 'share':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' link copied to clipboard.' : entities[0].title + ' copy link failed.';
              break;
            case 'reject':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' rejected.' : entities[0].title + ' reject failed.';
              break;
            case 'approve':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' approved.' : entities[0].title + ' approve failed.';
              break;
            case 'submit':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' submiited for approval.' : entities[0].title + ' submission failed.';
              break;
            case 'submit':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' submiited for approval.' : entities[0].title + ' submission failed.';
              break;
            case 'publish':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' is published.' : 'publish failed.';
              break;
            case 'download':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' downloaded.' : entities[0].title + ' download failed.';
              else message = entities.length + (response == '200' ? ' assets downloaded.' : ' assets download failed.');
              break;
            case 'add to collection':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' added to ' + relatedTo.title + '.' : entities[0].title + ' adding to ' + relatedTo.title + ' failed.';
              else
                message = entities.length + (response == '200' ? ' assets added to ' + relatedTo.title + '.' : ' assets adding to ' + relatedTo.title + ' failed.');
              break;
            case 'add to favorites':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' added to your favorites.' : entities[0].title + ' adding to your favorites failed.';
              else
                message = entities.length + (response == '200' ? ' assets added to your favorites.' : ' assets adding to your favorites failed.');
              break;
            case 'remove from favorites':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' removed from your favorites.' : entities[0].title + ' removing from your favorites failed.';
              else
                message = entities.length + (response == '200' ? ' assets removed from your favorites.' : ' assets removing from your favorites failed.');
              break;
          }
        } break;
      case 'collection':
        {
          switch (action) {
            case 'create':
              content = entities[0].title;
              message = response == '200' ? content + ' created.' : 'Failed to create a new collection.';
              break;
            case 'update':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' updated.' : 'Failed to update the collection.';
              break;
            case 'delete':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' deleted.' : 'Failed to delete the collection.';
              else message = response == '200' ? entities.length + ' collections deleted.' : 'Failed to delete ' + entities.length + ' collections.';
              break;
            case 'remove user':
              if (entities.length == 1)
                message = response == '200' ? entities[0].name + ' removed.' : 'Failed to remove user.';
              break;
            case 'share':
              if (entities.length == 1)
                message = response == '200' ? relatedTo.title + ' shared with ' + entities[0].name + '.' : 'Failed to share ' + relatedTo.title + ' with ' + entities[0].name + '.';
              else
                message = response == '200' ? relatedTo.title + ' shared with ' + entities.length + ' users.' : 'Failed to share ' + relatedTo.title + ' with ' + entities.length + ' users.';
              break;
            case 'add asset':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + 'added to ' + relatedTo.title + '.' : entities[0].title + ' adding to ' + relatedTo.title + ' failed.';
              else
                message = response == '200' ? entities.length + ' assets added to ' + relatedTo.title + '.' : entities.length + ' assets adding to ' + relatedTo.title + ' failed.';
              break;
            case 'remove asset':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' removed from ' + relatedTo.title + '.' : 'Failed to remove' + entities[0].title + ' from ' + relatedTo.title + '.';
              else
                message = response == '200' ? 'Removed ' + entities.length + ' assets from ' + relatedTo.title + '.' : 'Failed to remove ' + entities.length + ' assets from ' + relatedTo.title + '.';
              break;
            case 'download':
              message = response == '200' ? entities[0].title + ' download successful.' : 'Failed to download assets in ' + entities[0].title + '.';
              break;
          }
        }
        break;
      case 'guideline':
        {
          switch (action) {
            case 'create':
              message = response == '200' ? entities[0].title + ' created.' : 'Failed to create a new guideline.';
              break;
            case 'update':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' updated.' : 'Failed to update the guideline.';
              break;
            case 'delete':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' deleted.' : 'Failed to delete the guideline.';
              else message = response == '200' ? entities.length + ' guidelines deleted.' : 'Failed to delete ' + entities.length + ' guidelines.';
              break;
            case 'share':
              break;
            case 'add to favorites':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' added to your favorites.' : entities[0].title + ' adding to your favorites failed.';
              else
                message = entities.length + (response == '200' ? ' guideline added to your favorites.' : entities.length + ' guidelines adding to your favorites failed.');
              break;
            case 'remove from favorites':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' removed from your favorites.' : entities[0].title + ' remove from your favorites failed.';
              else
                message = entities.length + (response == '200' ? ' guidelines removed from your favorites.' : entities.length + ' guidelines removing from  your favorites failed.');
              break;
            case 'update':
              if (entities.length == 1)
                message = response == '200' ? entities[0].title + ' downloaded.' : 'Failed to download the guideline.';
              break;
            case '0':
            case 'inactive':
              message = response == '200' ? entities[0].title + ' is inactive.' : entities[0].title + ' de-activation failed.';
              break;
            case '1':
            case 'active':
              message = response == '200' ? entities[0].title + ' is active.' : entities[0].title + ' activation failed.';
              break;
            case 'download':
              message = response == '200' ? entities[0].title + ' download successful.' : 'Failed to download ' + entities[0].title + '.';
              break;
          }
        }
        break;
      case 'user': {
        switch (action) {
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' updated.' : 'Failed to update ' + entities[0].name + '.';
            break
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' deleted.' : 'Failed to remove ' + entities[0].name + '.';
            else message = response == '200' ? entities.length + ' users deleted.' : 'Failed to remove ' + entities.length + ' users.';
            break;
          case 'approve':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' approved.' : 'Failed to approve ' + entities[0].name + '.';
            else message = response == '200' ? entities.length + ' users approved.' : 'Failed to approve ' + entities.length + ' users.';
            break;
          case 'reject':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' rejected.' : 'Failed to reject ' + entities[0].name + '.';
            else message = response == '200' ? entities.length + ' users rejected.' : 'Failed to reject ' + entities.length + ' users.';
            break;
          case 'reset password link':
            message = response == '200' ? 'Reset password link sent.' : 'Failed to send reset password link.';
            break;
          case 'signup':
            message = response == '200' ? 'Signup successful.' : 'Failed to signup.';
            break;
          case 'register':
            message = response == '200' ? 'Registration successful.' : 'Failed to register.';
            break;
          case 'register':
            message = response == '200' ? 'Registration successful.' : 'Failed to register.';
            break;
          case '0':
          case 'inactive':
            message = response == '200' ? entities[0].name + ' is inactive.' : 'Failed to de-activate ' + entities[0].name + '.';
            break;
          case '1':
          case 'active':
            message = response == '200' ? entities[0].name + ' is active.' : 'Failed to activate ' + entities[0].name + '.';
            break;
          case 'import':
            if (entities.length == 1)
              message = response == '200' ? entities[0] + '  added.' : 'Failed to add <user-email>' + entities[0] + '.';
            else message = response == '200' ? entities.length + ' users added successfully.' : 'Failed to add ' + entities.length + ' users.';
            break;
        }
      } break;
      case 'my profile': {
        switch (action) {
          case 'update':
            message = response == '200' ? 'Saved your updated basic information.' : 'Failed to save your edits.';
            break
          case 'password change':
            message = response == '200' ? 'Password reset successful.' : 'Failed to change your password.';
            break;
          case 'profile change':
            message = response == '200' ? 'Profile picture updated.' : 'Failed to update your profile picture.';
            break;
        }
      } break;
      case 'asset group': {
        switch (action) {
          case 'create':
            content = entities[0].name;
            message = response == '200' ? content + ' created.' : 'Failed to create asset group.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' updated.' : 'Failed to update asset group.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' deleted.' : 'Failed to delete asset group.';
            else message = response == '200' ? entities.length + ' asset groups deleted.' : 'Failed to delete ' + entities.length + ' asset groups.';
            break;
        }
      } break;
      case 'category': {
        switch (action) {
          case 'create':
            content = entities[0].name;
            message = response == '200' ? content + ' created.' : 'Failed to create asset group category.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' updated.' : 'Failed to update asset group category.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' deleted.' : 'Failed to delete asset group category.';
            else message = response == '200' ? entities.length + ' asset group categories deleted.' : 'Failed to delete ' + entities.length + ' asset group categories.';
            break;
        }
      } break;
      case 'sub category': {
        switch (action) {
          case 'create':
            content = entities[0].name;
            message = response == '200' ? content + ' created.' : 'Failed to create asset sub-category.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' updated.' : 'Failed to update asset sub-category.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' deleted.' : 'Failed to delete asset sub-category.';
            else message = response == '200' ? entities.length + ' asset sub-categories deleted.' : 'Failed to delete ' + entities.length + ' asset sub-categories.';
            break;
        }
      } break;
      case 'tag': {
        switch (action) {
          case 'create':
            content = entities[0].name;
            message = response == '200' ? content + ' created.' : 'Failed to create a tag.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' updated.' : 'Failed to update the ' + entities[0].name + '.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].name + ' is removed successfully.' : 'Failed to remove the tag.';
            else message = response == '200' ? entities.length + ' tags deleted.' : 'Failed to remove the tag.';
            break;
        }
      } break;
      case 'template': {
        switch (action) {
          case 'upload':
            content = entities[0].title;
            message = response == '200' ? content + ' uploaded.' : 'Failed to upload a template.';
            break;
          case 'create':
            content = entities[0].title;
            message = response == '200' ? content + ' created.' : 'Failed to create a template.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' updated.' : 'Failed to update the ' + entities[0].title + '.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is removed.' : 'Failed to remove the template.';
            else message = response == '200' ? entities.length + ' templates deleted.' : 'Failed to remove the template.';
            break;
          case 'sumbit':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' submitted for approval.' : 'Failed to submit for approval.';
            break;
          case 'approve':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is approved.' : 'Failed to approve the template.';
            break;
          case 'approvewithrevision':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is approved with revision.' : 'Failed to approve the template.';
            break;
          case 'publish':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is published.' : 'Failed to publish the template.';
            break;
          case 'configSave':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' configuration is saved.' : 'Failed to save configuration.';
            break;
          case 'archive':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is archived.' : 'Failed to archive the template.';
            else message = response == '200' ? entities.length + ' templates archived.' : 'Failed to archive the selected templates.';
            break;
          case 'rejected':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is rejected.' : 'Failed to reject the template.';
            else message = response == '200' ? entities.length + ' templates rejected.' : 'Failed to reject the selected templates.';
            break;
        }
      } break;
      case 'order': {
        switch (action) {
          case 'upload':
            content = entities[0].title;
            message = response == '200' ? content + ' uploaded.' : 'Failed to upload a template.';
            break;
          case 'create':
            content = entities[0].title;
            message = response == '200' ? content + ' created.' : 'Failed to create a template.';
            break;
          case 'update':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' updated.' : 'Failed to update the ' + entities[0].title + '.';
            break;
          case 'delete':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is removed successfully.' : 'Failed to remove the template.';
            else message = response == '200' ? entities.length + ' templates deleted.' : 'Failed to remove the template.';
            break;
          case 'placeOrder':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' submitted for order.' : 'Failed to submit for order.';
            break;
          case 'approve':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is approved.' : 'Failed to approve.';
            break;
          case 'publish':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is published.' : 'Failed to publish.';
            break;
          case 'configSave':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' configuration is saved.' : 'Failed to save configuration.';
            break;
          case 'archive':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is archived successfully.' : 'Failed to archive the order.';
            else message = response == '200' ? entities.length + ' templates archived.' : 'Failed to archive the selected templates.';
            break;
          case 'rejected':
            if (entities.length == 1)
              message = response == '200' ? entities[0].title + ' is rejected.' : 'Failed to reject the order.';
            else message = response == '200' ? entities.length + ' templates rejected.' : 'Failed to reject the selected orders.';
            break;
        }
      } break;
    }
    return message;
  }

  static xmlStringToArray(xmlString) {
    let array = [];
    var parser = new DOMParser();
    let xml = parser.parseFromString(xmlString, "application/xml");
    let nodes = xml.querySelectorAll('*');
    for (let i = 0; i < nodes.length; i++) {
      let item = nodes[i];
      let obj = {
        name: '',
        displayName: '',
        value: '',
        type: '',
        editable: false,
        custamizable: false,
        profileField: false,
        collectionType: '',
        selectedCollections: [],
        configureStatus: false,
      }
      if (item.childNodes.length == 1 && item.childNodes[0].nodeType == 3) {
        obj = {
          name: item.nodeName,
          displayName: item.nodeName,
          value: item.textContent,
          type: 'text',
          editable: false,
          custamizable: false,
          profileField: false,
          collectionType: 'text',
          selectedCollections: [],
          configureStatus: false,
        }
        array.push(obj);
      }
      if (item.hasAttribute('href')) {
        obj = {
          name: item.nodeName,
          displayName: item.nodeName,
          value: item.getAttribute('href'),
          type: 'asset',
          editable: false,
          custamizable: false,
          profileField: false,
          collectionType: 'asset',
          selectedCollections: [],
          configureStatus: false,
        }
        array.push(obj);
      }
    }
    return array;
  }
  static createAssetGroupHierarchy(assetGroupList = []) {
    let assetGroup = assetGroupList.filter(a => a.parent == 0)
    assetGroup.forEach(element => {
      Utils.getChildrenOfAssetGroup(element, assetGroupList);
    });
    return assetGroup;

  }
  static getChildrenOfAssetGroup(assetGroup, assetGroupList = []) {
    let children = assetGroupList.filter(a => a.parent == assetGroup.id)
    assetGroup.children = children;
    if (assetGroup.children.length == 0)
      return;
    else
      children.forEach(element => {
        Utils.getChildrenOfAssetGroup(element, assetGroupList);
      });
  }
  static createTextLibrary(assetGroupList = []) {
    let assetGroup = assetGroupList.filter(a => a.parent == 0)
    assetGroup.forEach(element => {
      Utils.getChildrenOfTextLibrary(element, assetGroupList);
    });
    return assetGroup;

  }
  static getChildrenOfTextLibrary(assetGroup, assetGroupList = []) {
    let children = assetGroupList.filter(a => a.parent == assetGroup.textLibraryId)
    assetGroup.children = children;
    if (assetGroup.children.length == 0)
      return;
    else
      children.forEach(element => {
        Utils.getChildrenOfTextLibrary(element, assetGroupList);
      });
  }

  // to get proper date format for filters in brand activate
  static filterDateFormat(days, date?) {
    let currentDate = date ? new Date(date) : new Date();
    let lastDate = new Date(currentDate.getTime() - (days * 24 * 60 * 60 * 1000));
    let requiredFormat = lastDate.getFullYear() + "-" +
      ("00" + (lastDate.getMonth() + 1)).slice(-2) + "-" +
      ("00" + lastDate.getDate()).slice(-2) + " " +
      ("00" + lastDate.getHours()).slice(-2) + ":" +
      ("00" + lastDate.getMinutes()).slice(-2) + ":" +
      ("00" + lastDate.getSeconds()).slice(-2);

    return requiredFormat;
  }
}