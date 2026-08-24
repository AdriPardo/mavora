import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";

type PlaceholderPageProps = {
  title: string;
  description: string;
  emptyTitle: string;
  emptyDescription: string;
};

export function PlaceholderPage({
  title,
  description,
  emptyTitle,
  emptyDescription,
}: PlaceholderPageProps) {
  return (
    <div>
      <PageHeader title={title} description={description} />
      <EmptyState title={emptyTitle} description={emptyDescription} />
    </div>
  );
}
