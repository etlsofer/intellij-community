package com.intellij.openapi.vcs.changes.committed;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.update.AbstractCommonUpdateAction;
import com.intellij.openapi.vcs.update.ActionInfo;
import com.intellij.openapi.vcs.update.ScopeInfo;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.ui.Messages;

import java.util.*;

/**
 * @author yole
 */
public class GetCommittedChangelistAction extends AbstractCommonUpdateAction {
  public GetCommittedChangelistAction() {
    super(ActionInfo.UPDATE, CHANGELIST);
  }

  protected void actionPerformed(final VcsContext context) {
    Collection<FilePath> filePaths = getFilePaths(context);
    final List<ChangeList> selectedChangeLists = new ArrayList<ChangeList>();
    final ChangeList[] selectionFromContext = context.getSelectedChangeLists();
    if (selectionFromContext != null) {
      Collections.addAll(selectedChangeLists, selectionFromContext);
    }
    final List<CommittedChangeList> incomingChanges = CommittedChangesCache.getInstance(context.getProject()).getCachedIncomingChanges();
    final List<CommittedChangeList> intersectingChanges = new ArrayList<CommittedChangeList>();
    if (incomingChanges != null) {
      for(CommittedChangeList changeList: incomingChanges) {
        if (!selectedChangeLists.contains(changeList)) {
          for(Change change: changeList.getChanges()) {
            if (filePaths.contains(ChangesUtil.getFilePath(change))) {
              intersectingChanges.add(changeList);
              break;
            }
          }
        }
      }
    }
    if (intersectingChanges.size() > 0) {
      int rc = Messages.showOkCancelDialog(
        context.getProject(), VcsBundle.message("get.committed.changes.intersecting.prompt", intersectingChanges.size(), selectedChangeLists.size()),
        VcsBundle.message("get.committed.changes.title"), Messages.getQuestionIcon());
      if (rc != 0) return;
    }
    super.actionPerformed(context);
  }

  protected boolean filterRootsBeforeAction() {
    return false;
  }

  private static ScopeInfo CHANGELIST = new ScopeInfo() {
    public FilePath[] getRoots(final VcsContext context, final ActionInfo actionInfo) {
      final Collection<FilePath> filePaths = getFilePaths(context);
      return filePaths.toArray(new FilePath[filePaths.size()]);
    }

    public String getScopeName(final VcsContext dataContext, final ActionInfo actionInfo) {
      return "Changelist";
    }
  };

  private static Collection<FilePath> getFilePaths(final VcsContext context) {
    final Set<FilePath> files = new HashSet<FilePath>();
    final ChangeList[] selectedChangeLists = context.getSelectedChangeLists();
    if (selectedChangeLists != null) {
      for(ChangeList changelist: selectedChangeLists) {
        for(Change change: changelist.getChanges()) {
          files.add(ChangesUtil.getFilePath(change));
        }
      }
    }
    return files;
  }
}